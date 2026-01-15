package com.fResult.resilienceWalletLedger.common

import java.util.UUID

fun interface IdGenerator {
  fun generate(): UUID
}
