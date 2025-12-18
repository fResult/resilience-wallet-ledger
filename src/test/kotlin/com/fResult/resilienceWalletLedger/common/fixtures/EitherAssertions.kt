package com.fResult.resilienceWalletLedger.common.fixtures

import io.vavr.Function1.identity
import io.vavr.control.Either
import org.junit.jupiter.api.Assertions.fail

fun <L : Exception, R> Either<L, R>.expectRight(message: String): R {
  return this.fold(
    { left ->
      fail<Any>("$message but was failed with: [$left]")
      throw left
    },
    identity()
  )
}

fun <L : Exception, R> Either<L, R>.expectLeft(message: String): L {
  return this.fold(identity()) { right ->
    fail<L>("$message but was successful with: [$right]")
  }
}
