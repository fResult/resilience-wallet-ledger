package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.BankAccountId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WalletPersistenceAdapterTest {
  private val repository: SpringDataWalletRepository = mock(SpringDataWalletRepository::class.java)
  private val adapter = WalletPersistenceAdapter(repository)

  private val mockWalletId = WalletId.generate()
  private val ownerId = OwnerId.generate()
  private val bankAccountId = BankAccountId.generate()

  @Test
  fun `findById should return Right(Wallet) when entity exists`() {
    // Given
    val expectedResult = createWalletEntity(mockWalletId.value)
    given(repository.findById(mockWalletId.value)).willReturn(Mono.just(expectedResult))

    // When
    val actualResult = adapter.findById(mockWalletId)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val wallet = result.expectRight("Should find wallet")
        assertEquals(expectedResult.id, wallet.id.value)
        assertEquals(expectedResult.name, wallet.name)
        assertEquals(expectedResult.balanceAmount, wallet.balance.amount)
      }.verifyComplete()
  }

  private fun createWalletEntity(id: UUID) =
    WalletEntity(
      _id = id,
      name = "Test Wallet",
      balanceAmount = BigDecimal.TEN,
      balanceCurrency = Currency.USD.name,
      linkedBankAccountId = bankAccountId.value,
      ownerId = ownerId.value,
      status = "ACTIVE",
      version = 1L,
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
    )
}
