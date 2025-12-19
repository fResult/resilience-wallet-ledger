package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

class WalletInsufficientException(
  message: String? = "Insufficient balance in wallet",
  cause: Throwable? = null,
) : WalletException(message, cause)
