package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import java.util.UUID

// TODO: Refactor to use UUID Creator library for v7
@JvmInline
value class BankAccountId(
  val value: UUID,
) {
  companion object {
    fun generate(): BankAccountId = BankAccountId(UUID.randomUUID())
  }
}
