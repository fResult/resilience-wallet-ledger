package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.IdGenerator
import java.util.UUID

@JvmInline
value class BankAccountId(
  val value: UUID,
) {
  companion object {
    fun generate(): BankAccountId = BankAccountId(IdGenerator.generate())
  }
}
