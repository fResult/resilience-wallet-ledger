package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: Currency) {
  companion object {
    fun zero(currency: Currency) = Money(BigDecimal.ZERO, currency)
  }

  operator fun plus(other: Money): Money {
    require(this.currency == other.currency) {
      "Currency mismatch! Cannot add ${other.currency} to ${this.currency}"
    }

    return Money(this.amount.add(other.amount), this.currency)
  }

  operator fun minus(other: Money): Money {
    require(this.currency == other.currency) {
      "Currency mismatch! Cannot subtract ${other.currency} from ${this.currency}"
    }

    return Money(this.amount.subtract(other.amount), this.currency)
  }
}
