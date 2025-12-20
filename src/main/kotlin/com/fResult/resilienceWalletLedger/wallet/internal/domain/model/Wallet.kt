package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletInsufficientException
import io.vavr.control.Either
import java.util.UUID

data class Wallet(
  val id: UUID,
  val name: String,
  val balance: Money,
  val linkedBankAccountId: UUID,
  val ownerId: UUID,
  val status: WalletStatus,
  val version: Long = 0,
) {
  fun deposit(amount: Money): Either<WalletException, Wallet> {
    if (!amount.isPositive()) {
      return Either.left(
        WalletException(
          "Invalid deposit amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
        ),
      )
    }

    if (currencyMismatched(amount)) {
      return Either.left(
        WalletException(
          "Currency mismatch! Cannot deposit [${amount.currency}] to [${balance.currency}]",
        ),
      )
    }

    val balanceToDeposit = balance + amount

    return Either.right(this.copy(balance = balanceToDeposit, version = version + 1))
  }

  fun withdraw(amount: Money): Either<WalletException, Wallet> {
    if (!amount.isPositive()) {
      return Either.left(
        WalletException(
          "Invalid withdraw amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
        ),
      )
    }

    if (currencyMismatched(amount)) {
      return Either.left(
        WalletException(
          "Currency mismatch! Cannot withdraw [${amount.currency}] from [${balance.currency}]",
        ),
      )
    }
    if (isInsufficient(amount)) {
      return Either.left(
        WalletInsufficientException(
          "Insufficient Balance! Cannot withdraw ${amount.amount} ${amount.currency}",
        ),
      )
    }

    val balanceToWithdraw = balance - amount

    return Either.right(this.copy(balance = balanceToWithdraw, version = version + 1))
  }

  private fun currencyMismatched(money: Money): Boolean = money.currency != balance.currency

  private fun isInsufficient(money: Money): Boolean = balance.amount < money.amount
}
