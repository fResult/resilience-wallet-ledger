package com.fResult.resilienceWalletLedger.wallet.api

import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import reactor.core.publisher.Mono

interface WalletService {
  fun createWallet(
    walletName: String,
    ownerId: OwnerId,
    currency: Currency,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>>

  fun deposit(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>>

  fun withdraw(
    walletId: WalletId,
    amount: Money,
    refTransactionId: String,
  ): Mono<Either<WalletException, Pair<Wallet, List<WalletEvent>>>>
}
