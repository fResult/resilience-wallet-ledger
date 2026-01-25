package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.IdGenerator
import com.fResult.resilienceWalletLedger.common.extension.flatMapRight
import com.fResult.resilienceWalletLedger.common.extension.toEitherRight
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
import java.time.Instant

@Service
class WalletApplicationService(
  val walletRepository: WalletRepository,
  val idGenerator: IdGenerator,
) : WalletService {
  override fun createWallet(
    walletName: String,
    ownerId: OwnerId,
    currency: Currency,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>> =
    WalletId(idGenerator.generate())
      .let {
        Wallet.create(
          it,
          ownerId,
          walletName,
          currency,
          idGenerator.generate(),
          Instant.now(),
        )
      }.let(walletRepository::save)
      .toEitherRight { (wallet, events) ->
        /* Design note:
         * Publish domain events to notify other systems that a wallet was created
         * (e.g. reporting, notifications, compliance, downstream processes)
         */
        Either.right(Pair(wallet, events))
      }

  @Transactional
  override fun deposit(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>> =
    walletRepository
      .findById(walletId)
      .map { it.flatMap(depositing(amount, refTransactionId)) }
      .flatMap(::persist)

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
        Instant.now(),
      )
    }

  private fun depositing(
    amount: Money,
    idempotencyKey: String,
  ): (Wallet) -> Either<WalletException, Pair<Wallet, List<WalletEvent>>> =
    {
      it
        // FIXME: Inject eventId, refTransactionId, occurredOn from method parameters
        .deposit(
          amount,
          idGenerator.generate(),
          idempotencyKey,
          Instant.now(),
        ).map { (wallet, events) ->
          /* Design Note:
           * Publish domain events to notify other systems that a wallet was created
           * (e.g. reporting, notifications, compliance, downstream processes)
           */
          Pair(wallet, events)
        }
    }

  private fun persist(
    walletOrError: Either<WalletException, Pair<Wallet, List<WalletEvent>>>,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>> =
    walletOrError.fold(
      /*
       * TODO: [Outbox]
       * 1. Create `DepositFailedEvent` (`eventId`, `walletId`, `amount`, `reason`)
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
