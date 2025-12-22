package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either

data class Wallet(
  val id: WalletId,
  val name: String,
  val balance: Money,
  val linkedBankAccountId: BankAccountId?,
  val ownerId: OwnerId,
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
        WalletBalanceInsufficientException(
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
