package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import io.vavr.control.Either
import reactor.core.publisher.Mono

class WalletService(
  walletRepository: WalletRepository,
) {
  fun createWallet(
    ownerId: OwnerId,
    currency: Currency,
  ): Mono<Either<WalletException, Wallet>> {
    TODO("Not yet implemented")
  }
}
