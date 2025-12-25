package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.error.BusinessRuleViolation

class WalletBalanceInsufficientException(
  message: String? = "Insufficient balance in wallet",
  cause: Throwable? = null,
) : WalletException(message, cause),
  BusinessRuleViolation
