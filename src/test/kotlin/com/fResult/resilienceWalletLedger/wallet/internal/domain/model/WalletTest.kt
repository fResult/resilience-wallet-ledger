package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletInsufficientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.math.BigDecimal
import java.util.*

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
    val expectedErrorMessage = "Currency mismatch! Cannot deposit [${Currency.THB}] to [${Currency.USD}]"
    val initialWallet = createActiveUsdWallet(100)
    val depositAmount = thb(1000)

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
    val actualResult = result.expectRight("Withdraw should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `withdraw different currency should fail`() {
    // Given
    val expectedErrorMessage = "Currency mismatch! Cannot withdraw [${Currency.THB}] from [${Currency.USD}]"
    val initialWallet = createActiveUsdWallet(100)
    val withdrawalAmount = thb(1000)

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `withdraw over amount than balance should fail`() {
    // Given
    val balance = 100
    val amountToWithdraw = 1000
    val expectedErrorMessage = "Insufficient Balance! Cannot withdraw $amountToWithdraw ${Currency.USD}"
    val initialWallet = createActiveUsdWallet(balance)
    val withdrawalAmount = usd(amountToWithdraw)

    // When
    val result = initialWallet.withdraw(withdrawalAmount)

    // Then
    val actualError = result.expectLeft("Withdrawal should fail")
    assertInstanceOf<WalletInsufficientException>(actualError, "Error should be ${WalletInsufficientException::class.simpleName}")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  private fun createActiveUsdWallet(amount: Int): Wallet = Wallet(
    UUID.randomUUID(),
    "USD for investment",
    usd(amount),
    UUID.randomUUID(),
    UUID.randomUUID(),
    WalletStatus.ACTIVE,
  )

  private fun usd(amount: Int): Money = Money(BigDecimal(amount), Currency.USD)
  private fun thb(amount: Int): Money = Money(BigDecimal(amount), Currency.THB)
}
