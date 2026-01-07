package com.fResult.resilienceWalletLedger.common.event

import java.time.Instant

sealed interface DomainEvent {
  val occurredOn: Instant
}
