package com.fResult.resilienceWalletLedger.common.event

import java.time.Instant

interface DomainEvent {
  val occurredOn: Instant
}
