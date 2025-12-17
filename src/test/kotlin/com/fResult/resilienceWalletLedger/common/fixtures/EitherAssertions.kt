package com.fResult.resilienceWalletLedger.common.fixtures

import io.vavr.control.Either
import org.junit.jupiter.api.Assertions.assertTrue

fun <L, T> Either<L, T>.expectRight(message: String): T {
  assertTrue(this.isRight, "$message but failed with [${this.swap().getOrNull()}]")

  return this.get()
}
