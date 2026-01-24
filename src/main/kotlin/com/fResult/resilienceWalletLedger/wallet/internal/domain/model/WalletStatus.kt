package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

enum class WalletStatus(
  val displayName: String,
) {
  ACTIVE("Active"),
  INACTIVE("Inactive"),
  SUSPENDED("Suspended"),
}
