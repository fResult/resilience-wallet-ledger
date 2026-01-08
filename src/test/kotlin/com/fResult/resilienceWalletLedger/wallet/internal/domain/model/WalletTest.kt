package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletBalanceInsufficientException
import java.math.BigDecimal
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows

class WalletTest {
  private val walletId = WalletId.generate()
  private val ownerId = OwnerId.generate()
  private val linkedBankAccountId = BankAccountId.generate()

  @Test
  fun `deposit positive amount should increase balance and version`() {
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

  @Test
  fun `deposit zero amount should fail`() {
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
  fun `deposit negative amount should fail`() {
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
  fun `withdraw positive amount should decrease balance and increase version`() {
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
  fun `withdraw zero amount should fail`() {
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
  fun `withdraw negative amount should fail`() {
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
