package com.fResult.resilienceWalletLedger.wallet.internal.application.port.out

import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletResult
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletWithEvents
import io.vavr.control.Either
import reactor.core.publisher.Mono

interface WalletRepository {
  fun findById(id: WalletId): Mono<Either<WalletException, Wallet>>

  fun save(data: Pair<Wallet, List<WalletEvent>>): Mono<WalletResult<WalletWithEvents>>
}
