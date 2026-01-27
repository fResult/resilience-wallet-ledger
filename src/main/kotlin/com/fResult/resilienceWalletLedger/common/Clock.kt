package com.fResult.resilienceWalletLedger.common

import java.time.Instant

fun interface Clock {
  fun now(): Instant
}
