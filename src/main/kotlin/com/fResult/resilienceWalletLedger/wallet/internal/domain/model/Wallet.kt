package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.extension.toLeft
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyDeposited
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyWithdrawn
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletCreated
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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
  companion object {
    fun create(
      walletId: WalletId,
      ownerId: OwnerId,
      name: String,
      currency: Currency,
      eventId: UUID,
      occurredOn: Instant,
    ): Pair<Wallet, List<WalletEvent>> =
      Wallet(
        id = walletId,
        name = name,
        balance = Money(BigDecimal.ZERO, currency),
        ownerId = ownerId,
        status = WalletStatus.ACTIVE,
        createdAt = occurredOn,
        version = 0L,
        linkedBankAccountId = null,
      ).let(
        pairWithEvents(
          WalletCreated(
            eventId = eventId,
            walletId = walletId,
            ownerId = ownerId,
            linkedBankAccountId = null,
            name = name,
            initialBalance = Money(BigDecimal.ZERO, currency),
            occurredOn = occurredOn,
          ),
        ),
      )

    private fun pairWithEvents(event: WalletEvent): (Wallet) -> Pair<Wallet, List<WalletEvent>> =
      { wallet -> wallet to listOf(event) }
  }

  fun deposit(
    amount: Money,
    eventId: UUID,
    refTransactionId: String,
    occurredOn: Instant,
  ): Either<WalletException, Pair<Wallet, List<WalletEvent>>> {
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

    val balanceToUpdate = balance + amount
    val event =
      MoneyDeposited(
        eventId = eventId,
        currentBalance = balanceToUpdate,
        amount = amount,
        refTransactionId = refTransactionId,
        occurredOn = occurredOn,
      )

    return Either.right(this.copy(balance = balanceToUpdate) to listOf(event))
  }

  fun withdraw(
    amount: Money,
    eventId: UUID,
    refTransactionId: String,
    occurredOn: Instant,
  ): Either<WalletException, Pair<Wallet, List<WalletEvent>>> {
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

    val balanceToUpdate = balance - amount
    val event =
      MoneyWithdrawn(
        eventId = eventId,
        currentBalance = balanceToUpdate,
        amount = amount,
        refTransactionId = refTransactionId,
        occurredOn = occurredOn,
      )

    return Either.right(this.copy(balance = balanceToUpdate) to listOf(event))
  }

  private fun currencyMismatched(money: Money): Boolean = money.currency != balance.currency

  private fun hasInsufficientFunds(money: Money): Boolean = balance.amount < money.amount
}
