package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: Currency)
