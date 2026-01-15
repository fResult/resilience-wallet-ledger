package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows

class WalletTest {
  private val walletId = WalletId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
  private val ownerId = OwnerId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
  private val linkedBankAccountId =
    BankAccountId(UUID.fromString("00000000-0000-0000-0000-000000000003"))

  @Test
  fun `deposit with positive amount should succeed and increase balance`() {
    // Given
    val initialWallet = createActiveUsdWallet(1000)
    val expectedResult = initialWallet.copy(balance = usd(1100))
    val depositAmount = usd(100)

    // When
    val result = initialWallet.deposit(depositAmount)

    // Then
    val actualResult = result.expectRight("Deposit should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `deposit should reject different currency`() {
    // Given
    val expectedErrorMessage =
      "Currency mismatch! Cannot deposit [${Currency.THB}] to [${Currency.USD}]"
    val initialWallet = createActiveUsdWallet(100)
    val depositAmount = thb(1000)

    // When
    val result = initialWallet.deposit(depositAmount)

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

    // When
    val result = initialWallet.deposit(depositAmount)

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
    val initialWallet = createActiveUsdWallet(1000)
    val expectedResult = initialWallet.copy(balance = usd(900))
    val withdrawalAmount = usd(100)

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

    // Then
    val actualResult = result.expectRight("Withdrawal should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `withdraw should reject zero amount`() {
    // Given
    val invalidAmount = 0
    val expectedErrorMessage =
      "Invalid withdraw amount: [$invalidAmount ${Currency.USD}]. Must be greater than zero"
    val initialWallet = createActiveUsdWallet(100)
    val depositAmount = usd(invalidAmount)

    // When
    val result = initialWallet.withdraw(depositAmount)

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

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

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

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertInstanceOf<WalletBalanceInsufficientException>(
      actualError,
      "Error should be ${WalletBalanceInsufficientException::class.simpleName}",
    )
    assertEquals(expectedErrorMessage, actualError.message)
  }

  private fun createActiveUsdWallet(amount: Int): Wallet = createActiveWallet(usd(amount))

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
