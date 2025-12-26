package com.fResult.resilienceWalletLedger.common.exception

open class UnreachableException(
  message: String? = "Unreachable",
  cause: Throwable? = null,
) : RuntimeException(message, cause)
