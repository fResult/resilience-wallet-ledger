package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import java.util.UUID

// TODO: Refactor to use UUID Creator library for v7
@JvmInline
value class WalletId(
  val value: UUID,
) {
  companion object {
    fun generate(): WalletId = WalletId(UUID.randomUUID())
  }
}
