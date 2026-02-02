package com.fResult.resilienceWalletLedger.wallet.internal.domain.command

import java.time.Instant
import java.util.UUID

sealed interface WalletCommand {
  val eventId: UUID
  val occurredOn: Instant
}
