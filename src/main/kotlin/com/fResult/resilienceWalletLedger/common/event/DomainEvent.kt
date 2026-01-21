package com.fResult.resilienceWalletLedger.common.event

import java.time.Instant
import java.util.UUID

interface DomainEvent {
  val eventId: UUID
  val occurredOn: Instant
}
