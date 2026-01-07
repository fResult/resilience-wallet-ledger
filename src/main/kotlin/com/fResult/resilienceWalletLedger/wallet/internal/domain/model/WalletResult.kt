package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.event.DomainEvent

data class WalletResult(
  val wallet: Wallet,
  val events: List<DomainEvent>,
)
