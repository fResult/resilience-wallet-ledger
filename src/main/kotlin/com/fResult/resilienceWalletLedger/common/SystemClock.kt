package com.fResult.resilienceWalletLedger.common

import java.time.Instant
import org.springframework.stereotype.Component

@Component
class SystemClock : Clock {
  override fun now(): Instant = Instant.now()
}
