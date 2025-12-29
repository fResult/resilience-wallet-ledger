package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either
import java.math.BigDecimal

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
      return WalletException(
        "Invalid deposit amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
      ).let(toLeft)
    }

    if (currencyMismatched(amount)) {
      return WalletException(
        "Currency mismatch! Cannot deposit [${amount.currency}] to [${balance.currency}]",
      ).let(toLeft)
    }

    val balanceToDeposit = balance + amount

    return Either.right(this.copy(balance = balanceToDeposit, version = version + 1))
  }

  fun withdraw(amount: Money): Either<WalletException, Wallet> {
    if (!amount.isPositive()) {
      return WalletException(
        "Invalid withdraw amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
      ).let(toLeft)
    }

    if (currencyMismatched(amount)) {
      return WalletException(
        "Currency mismatch! Cannot withdraw [${amount.currency}] from [${balance.currency}]",
      ).let(toLeft)
    }
    if (isInsufficient(amount)) {
      return WalletBalanceInsufficientException(
        "Insufficient Balance! Cannot withdraw ${amount.amount} ${amount.currency}",
      ).let(toLeft)
    }

    val balanceToWithdraw = balance - amount

    return Either.right(this.copy(balance = balanceToWithdraw, version = version + 1))
  }

  private val toLeft: (WalletException) -> Either<WalletException, Wallet> = { Either.left(it) }

  private fun currencyMismatched(money: Money): Boolean = money.currency != balance.currency

  private fun isInsufficient(money: Money): Boolean = balance.amount < money.amount

  companion object {
    fun create(
      ownerId: OwnerId,
      name: String,
      currency: Currency,
    ) = Wallet(
      id = WalletId.generate(),
      name = name,
      balance = Money(BigDecimal.ZERO, currency),
      ownerId = ownerId,
      status = WalletStatus.ACTIVE,
      version = 0L,
      linkedBankAccountId = null,
    )
  }
}
