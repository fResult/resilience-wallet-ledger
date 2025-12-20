package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.exception.BusinessRuleViolation

class WalletInsufficientException(
  message: String? = "Insufficient balance in wallet",
  cause: Throwable? = null,
) : WalletException(message, cause),
  BusinessRuleViolation
