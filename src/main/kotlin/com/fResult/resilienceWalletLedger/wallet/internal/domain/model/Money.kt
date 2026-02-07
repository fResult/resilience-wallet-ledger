package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.exception.InvariantViolationException
import com.fasterxml.jackson.annotation.JsonIgnore
import io.vavr.control.Either
import java.math.BigDecimal

// Act as a Value Object
data class Money(
  val amount: BigDecimal,
  val currency: Currency,
) {
  init {
    require(amount >= BigDecimal.ZERO) {
      "Money amount must be non-negative, but got: $amount"
    }
  }

  companion object {
    fun of(
      amount: BigDecimal,
      currency: Currency,
    ): Either<InvariantViolationException, Money> {
      if (amount < BigDecimal.ZERO) {
        return Either.left(
          InvariantViolationException("Currency cannot be smaller than zero, but got $amount"),
        )
      }

      return Either.right(Money(amount, currency))
    }

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

  @JsonIgnore
  fun isPositive() = amount > BigDecimal.ZERO
}
