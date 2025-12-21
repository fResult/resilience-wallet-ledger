package com.fResult.resilienceWalletLedger.wallet.internal.application.port.out

import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import reactor.core.publisher.Mono

interface WalletRepository {
  fun findById(id: WalletId): Mono<Either<WalletException, Wallet>>

  fun save(wallet: Wallet): Mono<Either<WalletException, Wallet>>
}
