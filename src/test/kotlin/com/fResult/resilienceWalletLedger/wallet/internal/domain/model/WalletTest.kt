package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletInsufficientException
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class WalletTest {
  @Test
  fun `deposit positive amount should increase balance and version`() {
    // Given
    val initialWallet = createActiveUsdWallet(1000)
    val expectedResult = initialWallet.copy(balance = usd(1100), version = 1)
    val depositAmount = usd(100)

    // When
    val result = initialWallet.deposit(depositAmount)

    // Then
    val actualResult = result.expectRight("Deposit should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `deposit different currency should fail`() {
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

  @ParameterizedTest
  @CsvSource("-1", "0")
  fun `deposit zero or negative amount should fail`(invalidAmount: Int) {
    // Given
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
  fun `withdraw positive amount should decrease balance and increase version`() {
    // Given
    val initialWallet = createActiveUsdWallet(1000)
    val expectedResult = initialWallet.copy(balance = usd(900), version = 1)
    val withdrawalAmount = usd(100)

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

    // Then
    val actualResult = result.expectRight("Withdrawal should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @ParameterizedTest
  @CsvSource("-1", "0")
  fun `withdraw zero or negative amount should fail`(invalidAmount: Int) {
    // Given
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
  fun `withdraw different currency should fail`() {
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
  fun `withdraw insufficient funds should fail`() {
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
    assertInstanceOf<WalletInsufficientException>(
      actualError,
      "Error should be ${WalletInsufficientException::class.simpleName}",
    )
    assertEquals(expectedErrorMessage, actualError.message)
  }

  private fun createActiveUsdWallet(amount: Int): Wallet = createActiveWallet(usd(amount))

  private fun createActiveWallet(balance: Money) =
    Wallet(
      id = UUID.randomUUID(),
      name = "Test Wallet",
      balance = balance,
      linkedBankAccountId = UUID.randomUUID(),
      ownerId = UUID.randomUUID(),
      WalletStatus.ACTIVE,
    )

  private fun usd(amount: Int): Money = Money(BigDecimal(amount), Currency.USD)

  private fun thb(amount: Int): Money = Money(BigDecimal(amount), Currency.THB)
}
