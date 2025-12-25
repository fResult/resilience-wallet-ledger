package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.error.ResourceNotFound

class WalletNotFoundException(
  message: String? = "Wallet not found",
  cause: Throwable? = null,
) : WalletException(message, cause),
  ResourceNotFound
