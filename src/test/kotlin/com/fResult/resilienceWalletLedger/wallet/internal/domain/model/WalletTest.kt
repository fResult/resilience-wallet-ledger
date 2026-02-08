package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.command.DepositCommand
import com.fResult.resilienceWalletLedger.wallet.internal.domain.command.WithdrawalCommand
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyDeposited
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyWithdrawn
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows

class WalletTest {
  private val walletId = WalletId(UUID.fromString("019c0887-990e-7c44-842e-e6cb2f53d5ac"))
  private val ownerId = OwnerId(UUID.fromString("019c088e-6a14-7d23-837c-ca3b05033a0a"))
  private val linkedBankAccountId =
    BankAccountId(UUID.fromString("019c29c5-fda1-7d56-b290-ed0c4afbeeb8"))
  private val fixedEventUuid = UUID.fromString("019c088a-f22e-7009-9e51-9694ea8cbfa8")
  private val fixedIdempotencyKey = UUID.fromString("019c088d-9968-7e1f-9a93-01a0b5d02d98")

  @Test
  fun `deposit with positive amount should succeed and increase balance`() {
    // Given
    val initialBalance = usd(1000)
    val depositAmount = usd(100)
    val expectedBalance = initialBalance + depositAmount
    val initialWallet = createActiveWallet(initialBalance)
    val depositCommand = createDepositCommand(depositAmount, Instant.now())
    val expectedEvent =
      MoneyDeposited(
        fixedEventUuid,
        depositCommand.amount,
        expectedBalance,
        fixedIdempotencyKey.toString(),
        depositCommand.occurredOn,
      )
    val expectedWallet = initialWallet.copy(balance = expectedBalance)

    // When
    val result = initialWallet.deposit(depositCommand)

    // Then
    val (actualWallet, actualEvents) = result.expectRight("Deposit should succeed")
    assertEquals(expectedWallet, actualWallet)
    assertEquals(listOf(expectedEvent), actualEvents)
  }

  @Test
  fun `deposit should reject different currency`() {
    // Given
    val expectedErrorMessage =
      "Currency mismatch! Cannot deposit [${Currency.THB}] to [${Currency.USD}]"
    val initialWallet = createActiveUsdWallet(300)
    val depositAmount = thb(1000)
    val depositCommand = createDepositCommand(depositAmount, Instant.now())

    // When
    val result =
      initialWallet.deposit(depositCommand)

    // Then
    val actualError = result.expectLeft("Deposit should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `deposit should reject zero amount`() {
    // Given
    val invalidAmount = 0
    val expectedErrorMessage =
      "Invalid deposit amount: [$invalidAmount ${Currency.USD}]. Must be greater than zero"
    val initialWallet = createActiveUsdWallet(100)
    val depositAmount = usd(invalidAmount)
    val depositCommand = createDepositCommand(depositAmount, Instant.now())

    // When
    val result = initialWallet.deposit(depositCommand)

    // Then
    val actualError = result.expectLeft("Deposit should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `deposit should throw exception when amount is negative`() {
    // Given
    val invalidAmount = -1
    val expectedErrorMessage = "Money amount must be non-negative, but got: $invalidAmount"

    // When
    val executable: () -> Unit = { thb(invalidAmount) }

    // Then
    val actualError = assertThrows<IllegalArgumentException>(executable)
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `withdraw with positive amount should succeed and decrease balance`() {
    // Given
    val initialBalance = usd(1000)
    val withdrawalAmount = usd(100)
    val withdrawalCommand = createWithdrawalCommand(withdrawalAmount, Instant.now())
    val initialWallet = createActiveWallet(initialBalance)
    val expectedBalance = initialBalance - withdrawalAmount
    val expectedWallet = initialWallet.copy(balance = expectedBalance)
    val expectedEvent =
      MoneyWithdrawn(
        fixedEventUuid,
        withdrawalAmount,
        expectedBalance,
        fixedIdempotencyKey.toString(),
        withdrawalCommand.occurredOn,
      )

    // When
    val result = initialWallet.withdraw(withdrawalCommand)

    // Then
    val (actualWallet, actualEvents) = result.expectRight("Withdrawal should succeed")
    assertEquals(expectedWallet, actualWallet)
    assertEquals(listOf(expectedEvent), actualEvents)
  }

  @Test
  fun `withdraw should reject zero amount`() {
    // Given
    val invalidAmount = 0
    val expectedErrorMessage =
      "Invalid withdraw amount: [$invalidAmount ${Currency.USD}]. Must be greater than zero"
    val initialWallet = createActiveUsdWallet(100)
    val withdrawalAmount = usd(invalidAmount)
    val withdrawalCommand = createWithdrawalCommand(withdrawalAmount, Instant.now())

    // When
    val result = initialWallet.withdraw(withdrawalCommand)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `withdraw should throw exception when amount is negative`() {
    // Given
    val invalidAmount = -1
    val expectedErrorMessage = "Money amount must be non-negative, but got: $invalidAmount"

    // When
    val executable: () -> Unit = { usd(invalidAmount) }

    // Then
    val actualException = assertThrows<IllegalArgumentException>(executable)
    assertEquals(expectedErrorMessage, actualException.message)
  }

  @Test
  fun `withdraw should reject different currency`() {
    // Given
    val expectedErrorMessage =
      "Currency mismatch! Cannot withdraw [${Currency.THB}] from [${Currency.USD}]"
    val initialWallet = createActiveUsdWallet(100)
    val withdrawalAmount = thb(1000)
    val withdrawalCommand = createWithdrawalCommand(withdrawalAmount, Instant.now())

    // When
    val result = initialWallet.withdraw(withdrawalCommand)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `withdraw should reject insufficient funds`() {
    // Given
    val initialBalanceAmount = 100
    val withdrawAmountVal = 1000
    val expectedErrorMessage =
      "Insufficient Balance! Cannot withdraw $withdrawAmountVal ${Currency.USD}"
    val initialWallet = createActiveUsdWallet(initialBalanceAmount)
    val withdrawalAmount = usd(withdrawAmountVal)
    val withdrawalCommand = createWithdrawalCommand(withdrawalAmount, Instant.now())

    // When
    val result = initialWallet.withdraw(withdrawalCommand)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertInstanceOf<WalletBalanceInsufficientException>(
      actualError,
      "Error should be ${WalletBalanceInsufficientException::class.simpleName}",
    )
    assertEquals(expectedErrorMessage, actualError.message)
  }

  private fun createActiveUsdWallet(amount: Int): Wallet = createActiveWallet(usd(amount))

  fun createDepositCommand(
    amount: Money,
    occurredOn: Instant,
  ) = DepositCommand(amount, fixedIdempotencyKey.toString(), fixedEventUuid, occurredOn)

  fun createWithdrawalCommand(
    amount: Money,
    occurredOn: Instant,
  ) = WithdrawalCommand(amount, fixedIdempotencyKey.toString(), fixedEventUuid, occurredOn)

  private fun createActiveWallet(balance: Money) =
    Wallet(
      id = walletId,
      name = "Test Wallet",
      balance = balance,
      linkedBankAccountId = linkedBankAccountId,
      ownerId = ownerId,
      status = WalletStatus.ACTIVE,
      createdAt = Instant.now(),
      version = 0,
    )

  private fun usd(amount: Int): Money = Money(BigDecimal(amount), Currency.USD)

  private fun thb(amount: Int): Money = Money(BigDecimal(amount), Currency.THB)
}
