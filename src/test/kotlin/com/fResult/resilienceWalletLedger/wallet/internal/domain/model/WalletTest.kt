package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertTrue

class WalletTest {
  @Test
  fun `deposit when amount is positive, then deposit completed`() {
    val wallet = createActiveWallet(1000)
    val expectedResult = wallet.copy(balance = usd(1100), version = 1)
    val balanceToDeposit = usd(100)

    val result = wallet.deposit(balanceToDeposit)

    assertTrue("Deposit should succeed but failed with: ${result.swap().orNull}") {
      result.isRight
    }
    val actualResult = result.get()
    assertEquals(expectedResult, actualResult)
  }

  private fun createActiveWallet(amount: Int): Wallet = Wallet(
    UUID.randomUUID(),
    "USD for investment",
    usd(amount),
    UUID.randomUUID(),
    UUID.randomUUID(),
    WalletStatus.ACTIVE,
  )

  private fun usd(amount: Int): Money = Money(BigDecimal(amount), Currency.USD)
}
