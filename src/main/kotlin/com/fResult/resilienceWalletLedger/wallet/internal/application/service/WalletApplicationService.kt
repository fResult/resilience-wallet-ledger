package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.Clock
import com.fResult.resilienceWalletLedger.common.IdGenerator
import com.fResult.resilienceWalletLedger.common.extension.flatMapRight
import com.fResult.resilienceWalletLedger.common.extension.toLeft
import com.fResult.resilienceWalletLedger.wallet.api.WalletService
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
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
    val (wallet, events) =
      Wallet.create(
        walletId,
        ownerId,
        walletName,
        currency,
        eventId,
        now,
      )

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

  private fun processDepositFor(
    amount: Money,
    refTransactionId: String,
  ): (Wallet) -> Mono<Either<WalletException, Wallet>> =
    { wallet ->
      val eventId = idGenerator.generate()
      val now = clock.now()
      val result = wallet.deposit(amount, eventId, refTransactionId, now)

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

  @Transactional
  override fun withdraw(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>> =
    walletRepository
      .findById(walletId)
      .map { it.flatMap(withdrawing(amount, refTransactionId)) }
      .flatMap(::persist)

  private fun withdrawing(
    amount: Money,
    refTransactionId: String,
  ): (Wallet) -> Either<WalletException, Pair<Wallet, List<WalletEvent>>> =
    { wallet ->
      // FIXME: Inject eventId, refTransactionId, occurredOn from method parameters
      wallet.withdraw(
        amount,
        idGenerator.generate(),
        refTransactionId,
        clock.now(),
      )
    }
          amount,
          idGenerator.generate(),
          clock.now(),
    }

  private fun persist(
    walletOrError: Either<WalletException, Pair<Wallet, List<WalletEvent>>>,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>> =
    walletOrError.fold(
      /*
       * TODO: [Outbox]
       * 1. Create `MoneyDepositFailed` event (with `eventId`, `walletId`, `amount`, `reason`)
       * 2. Save event to Outbox Repository (Must commit transaction, DON'T rollback)
       */
      { Mono.just(it.toLeft()) },
      { (wallet, events) ->
        walletRepository
          .save(wallet to events)
          .flatMapRight { (wallet, events) -> Mono.just(Either.right(wallet to events)) }
      },
    )
}
