package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MoneyTest {
  @ParameterizedTest
  @CsvSource("0", "100", "1000")
  fun `should allow creation with non-negative amount`(amount: Int) {
    // Given
    val expectedResult = jpy(amount)

    // When
    val result = Money.of(BigDecimal(amount), Currency.JPY)

    // Then
    val actualResult = result.expectRight("Create money should succeed")
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `should throw exception when created with negative amount`() {
    // Given
    val invalidAmount = -1
    val expectedErrorMessage = "Money amount must be non-negative, but got: $invalidAmount"

    // When
    val actualResult: () -> Unit = { jpy(invalidAmount) }

    // Then
    val actualError = assertThrows<IllegalArgumentException>(actualResult)
    assertEquals(expectedErrorMessage, actualError.message)
  }

  @Test
  fun `Money#zero factory should create Money with zero amount`() {
    // Given
    val expectedResult = jpy(0)

    // When
    val actualResult = Money.zero(Currency.JPY)

    // Then
    assertEquals(expectedResult, actualResult)
  }

  @Test
  fun `Money#of factory should reject negative amount`() {
    // Given
    val invalidAmount = -1
    val expectedErrorMessage = "Currency cannot be smaller than zero, but got $invalidAmount"

    // When
    val actualResult = Money.of(BigDecimal(invalidAmount), Currency.JPY)

    // Then
    val actualError = actualResult.expectLeft("Create money should fail")
    assertEquals(expectedErrorMessage, actualError.message)
  }

  private fun jpy(amount: Int): Money = Money(BigDecimal(amount), Currency.JPY)
}
