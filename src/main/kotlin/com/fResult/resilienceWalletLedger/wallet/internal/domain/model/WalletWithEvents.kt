package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either

/**
 * A container holding the updated [Wallet] state and the [WalletEvent]s generated during the operation.
 * Used to return both the state and side effects from pure domain functions.
 */
data class WalletWithEvents(
  val wallet: Wallet,
  val events: List<WalletEvent>,
)

typealias WalletResult = Either<WalletException, WalletWithEvents>
