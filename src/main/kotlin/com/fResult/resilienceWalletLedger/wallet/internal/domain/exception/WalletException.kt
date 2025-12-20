package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

open class WalletException(
  message: String? = "Wallet Error",
  cause: Throwable? = null,
) : RuntimeException(message, cause)
