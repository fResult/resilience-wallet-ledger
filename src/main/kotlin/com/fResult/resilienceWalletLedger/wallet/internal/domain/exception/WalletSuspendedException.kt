package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.error.BusinessRuleViolation

open class WalletSuspendedException(
  message: String? = "Wallet Suspended",
  cause: Throwable? = null,
) : WalletException(message, cause),
  BusinessRuleViolation
