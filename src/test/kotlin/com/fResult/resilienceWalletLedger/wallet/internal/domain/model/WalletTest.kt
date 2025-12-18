package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertContains

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
    assertInstanceOf<WalletException>(actualError, "Deposit should fail with ${WalletException::class.simpleName}")
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
