package com.fResult.resilienceWalletLedger.wallet.internal.domain.exception

import com.fResult.resilienceWalletLedger.common.error.BusinessRuleViolation
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import java.math.BigDecimal

class WalletBalanceInsufficientException(
  message: String? = "Insufficient balance in wallet",
  cause: Throwable? = null,
) : WalletException(message, cause),
  BusinessRuleViolation {
  constructor(
    balance: BigDecimal,
    currency: Currency,
    required: BigDecimal,
    cause: Throwable? = null,
  ) : this(
    "Insufficient balance: required [$required $currency], but actual is [$balance $currency]",
    cause,
  )
}
