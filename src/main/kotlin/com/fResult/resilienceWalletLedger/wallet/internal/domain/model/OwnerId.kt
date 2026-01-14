package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.IdGenerator
import java.util.UUID

@JvmInline
value class OwnerId(
  val value: UUID,
) {
  companion object {
    fun generate(): OwnerId = OwnerId(IdGenerator.generate())
  }
}
