package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.error.ConcurrencyConflict

class WalletConcurrencyException(
  message: String = "Concurrency conflict detected, please retry",
  cause: Throwable?,
) : WalletException(message, cause),
  ConcurrencyConflict
