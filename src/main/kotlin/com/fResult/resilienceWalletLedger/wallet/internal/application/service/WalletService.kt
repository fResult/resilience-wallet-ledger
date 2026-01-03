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

  fun deposit(
    walletId: WalletId,
    amountToDeposit: Money,
  ): Mono<Either<WalletException, Wallet>> =
    walletRepository.findById(walletId).flatMap { findResult ->
      findResult
        .flatMap { existingWallet -> existingWallet.deposit(amountToDeposit) }
        .fold(
          { domainError ->
            // Naive Deposit Failed (Rule) Transformation
            val ex = WalletException(domainError.message)
            Mono.just(ex.toLeft())
          },
          walletRepository::save,
        )
    }
}
