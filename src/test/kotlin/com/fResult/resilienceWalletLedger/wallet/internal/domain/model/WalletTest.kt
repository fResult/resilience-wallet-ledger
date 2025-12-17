package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class WalletTest {
  @Test
  fun `deposit positive amount should increase balance and version`() {
    val initialWallet = createActiveWallet(1000)
    val expectedResult = initialWallet.copy(balance = usd(1100), version = 1)
    val depositAmount = usd(100)

    val result = initialWallet.deposit(depositAmount)

    val actualResult = result.expectRight("Deposit should succeed")
    assertEquals(expectedResult, actualResult)
  }

  fun `deposit different currency should be failed`() {

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
