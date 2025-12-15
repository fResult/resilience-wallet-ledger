package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

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
  fun deposit(amount: Money): Wallet {
    require(positiveAmount(amount)) {
      "Invalid amount: [${amount.amount} ${amount.currency.name}]. Amount must be greater than zero."
    }
    require(amount.currency == balance.currency) {
      "Currency mismatch! Cannot deposit ${amount.currency.name} into ${balance.currency.name}"
    }
    val balanceToDeposit = balance + amount

    return this.copy(balance = balanceToDeposit, version = version + 1)
  }

  fun withdraw(amount: Money): Wallet {
    require(positiveAmount(amount)) {
      "Invalid amount: [${amount.amount} ${amount.currency.name}]. Amount must be greater than zero."
    }
    require(amount.currency == balance.currency) {
      "Currency mismatch! Cannot withdraw ${amount.currency.value} from ${balance.currency.value}"
    }
    require(isSufficient(amount)) {
      "Insufficient Balance! Cannot withdraw ${amount.amount} ${amount.currency.value}"
    }
    val balanceToWithdraw = balance - amount

    return this.copy(balance = balanceToWithdraw, version = version + 1)
  }

  private fun positiveAmount(money: Money): Boolean = money.amount > 0.toBigDecimal()
  private fun isSufficient(money: Money): Boolean = balance.amount >= money.amount
}
