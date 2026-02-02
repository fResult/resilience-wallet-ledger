package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.Clock
import com.fResult.resilienceWalletLedger.common.IdGenerator
import com.fResult.resilienceWalletLedger.wallet.api.WalletService
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.command.CreateWalletCommand
import com.fResult.resilienceWalletLedger.wallet.internal.domain.command.DepositCommand
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class WalletApplicationService(
  val walletRepository: WalletRepository,
  val idGenerator: IdGenerator,
  val clock: Clock,
) : WalletService {
  override fun createWallet(
    walletName: String,
    ownerId: OwnerId,
    currency: Currency,
  ): Mono<Either<WalletException, Wallet>> {
    val walletId = WalletId(idGenerator.generate())
    val eventId = idGenerator.generate()
    val now = clock.now()
    val createWalletCommand =
      CreateWalletCommand(
        walletId,
        ownerId,
        walletName,
        currency,
        eventId,
        now,
      )
    val (wallet, events) = Wallet.create(createWalletCommand)

    return walletRepository
      .save(wallet to events)
      .map { result ->
        /* Design note:
         * Publish domain events to notify other systems that a wallet was created
         * (e.g., reporting, notifications, compliance, downstream processes)
         */
        result.map { (savedWallet, _) -> savedWallet }
      }
  }

  @Transactional
  override fun deposit(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Wallet>> =
    walletRepository
      .findById(walletId)
      .flatMap { walletOrError ->
        walletOrError.fold(
          { error -> Mono.just(Either.left(error)) },
          processDepositFor(amount, refTransactionId),
        )
      }

  @Transactional
  override fun withdraw(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Wallet>> =
    walletRepository
      .findById(walletId)
      .flatMap { walletOrError ->
        walletOrError.fold(
          { error -> Mono.just(Either.left(error)) },
          processWithdrawalFor(amount, refTransactionId),
        )
      }

  private fun processDepositFor(
    amount: Money,
    refTransactionId: String,
  ): (Wallet) -> Mono<Either<WalletException, Wallet>> =
    { wallet ->
      val eventId = idGenerator.generate()
      val now = clock.now()
      val depositCommand = DepositCommand(amount, refTransactionId, eventId, now)
      val result = wallet.deposit(depositCommand)

      result.fold(
        { error ->
          // Design Note: Future phase - Save `DepositFailed` event here
          Mono.just(Either.left(error))
        },
        { (walletToDeposit, events) ->
          walletRepository
            .save(walletToDeposit to events)
            .map { result ->
              result.map { (depositedWallet, _) -> depositedWallet }
            }
        },
      )
    }

  private fun processWithdrawalFor(
    amount: Money,
    refTransactionId: String,
  ): (Wallet) -> Mono<Either<WalletException, Wallet>> =
    { wallet ->
      val eventId = idGenerator.generate()
      val now = clock.now()

      wallet
        .withdraw(amount, eventId, refTransactionId, now)
        .fold(
          { error ->
            // Design Note: Future phase - Save `WithdrawalFailed` event here
            Mono.just(Either.left(error))
          },
          { (walletToWithdraw, events) ->
            walletRepository
              .save(walletToWithdraw to events)
              .map { result ->
                result.map { (withdrawnWallet, _) -> withdrawnWallet }
              }
          },
        )
    }
}
