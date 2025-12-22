package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.exception.BusinessRuleViolation

open class WalletAlreadyExistsException(
  message: String? = "Wallet already exists",
  cause: Throwable? = null,
) : WalletException(message, cause),
  BusinessRuleViolation
