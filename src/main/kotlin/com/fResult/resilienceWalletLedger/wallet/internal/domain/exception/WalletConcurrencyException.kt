package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.exception.ConcurrencyConflict

class WalletConcurrencyException(
  message: String = "Concurrency conflict detected, please retry",
  cause: Throwable?,
) : WalletException(message, cause),
  ConcurrencyConflict
