package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.extension.toLeft
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

class WalletService(
  val walletRepository: WalletRepository,
) {
  fun createWallet(
    walletName: String,
    ownerId: OwnerId,
    currency: Currency,
  ): Mono<Either<WalletException, Wallet>> =
    Wallet.create(ownerId, walletName, currency).let(walletRepository::save)

  @Transactional
  fun deposit(
    walletId: WalletId,
    amount: Money,
  ): Mono<Either<WalletException, Wallet>> =
    walletRepository.findById(walletId).flatMap { walletOrError ->
      walletOrError
        .flatMap(depositing(amount))
        .fold(
          { domainError ->
            /*
             * TODO: [Outbox]
             * 1. Create `DepositFailedEvent` (`eventId`, `walletId`, `amount`, `reason`)
             * 2. Save event to Outbox Repository (Must commit transaction, DON'T rollback)
             */
            Mono.just(domainError.toLeft())
          },
          // TODO: [Outbox] Save DepositCompletedEvent to Outbox Repository
          walletRepository::save,
        )
    }

  private fun depositing(amount: Money): (Wallet) -> Either<WalletException, Wallet> =
    { it.deposit(amount) }
}
