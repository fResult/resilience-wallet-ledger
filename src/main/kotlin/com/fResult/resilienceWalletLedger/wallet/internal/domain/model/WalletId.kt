package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.IdGenerator
import java.util.UUID

@JvmInline
value class WalletId(
  val value: UUID,
) {
  companion object {
    fun generate(): WalletId = WalletId(IdGenerator.generate())
  }
}
