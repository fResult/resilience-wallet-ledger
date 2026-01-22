package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.event.DomainEvent
import com.fResult.resilienceWalletLedger.common.extension.toLeft
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyDeposited
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either
import java.math.BigDecimal
import java.time.Instant

data class Wallet(
  val id: WalletId,
  val name: String,
  val balance: Money,
  val linkedBankAccountId: BankAccountId?,
  val ownerId: OwnerId,
  val status: WalletStatus,
  val createdAt: Instant,
  val version: Long = 0,
) {
  fun deposit(amount: Money): Either<WalletException, Pair<Wallet, List<DomainEvent>>> {
    if (!amount.isPositive()) {
      return WalletException(
        "Invalid deposit amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
      ).toLeft()
    }

    if (currencyMismatched(amount)) {
      return WalletException(
        "Currency mismatch! Cannot deposit [${amount.currency}] to [${balance.currency}]",
      ).toLeft()
    }

    val balanceToDeposit = balance + amount
    val event =
      MoneyDeposited(
        id.value,
        amount,
        balanceToDeposit,
        "",
        Instant.now(),
      )

    return Either.right(this.copy(balance = balanceToDeposit) to listOf(event))
  }

  fun withdraw(amount: Money): Either<WalletException, Wallet> {
    if (!amount.isPositive()) {
      return WalletException(
        "Invalid withdraw amount: [${amount.amount} ${amount.currency}]. Must be greater than zero",
      ).toLeft()
    }

    if (currencyMismatched(amount)) {
      return WalletException(
        "Currency mismatch! Cannot withdraw [${amount.currency}] from [${balance.currency}]",
      ).toLeft()
    }
    if (hasInsufficientFunds(amount)) {
      return WalletBalanceInsufficientException(
        "Insufficient Balance! Cannot withdraw ${amount.amount} ${amount.currency}",
      ).toLeft()
    }

    val balanceToWithdraw = balance - amount

    return Either.right(this.copy(balance = balanceToWithdraw))
  }

  private fun currencyMismatched(money: Money): Boolean = money.currency != balance.currency

  private fun hasInsufficientFunds(money: Money): Boolean = balance.amount < money.amount

  companion object {
    fun create(
      walletId: WalletId,
      ownerId: OwnerId,
      name: String,
      currency: Currency,
    ) = Wallet(
      id = walletId,
      name = name,
      balance = Money(BigDecimal.ZERO, currency),
      ownerId = ownerId,
      status = WalletStatus.ACTIVE,
      createdAt = Instant.now(),
      version = 0L,
      linkedBankAccountId = null,
    )
  }
}
