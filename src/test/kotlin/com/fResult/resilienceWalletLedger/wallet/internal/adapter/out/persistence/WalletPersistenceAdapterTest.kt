package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletAlreadyExistsException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletConcurrencyException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletNotFoundException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.BankAccountId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertContains
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WalletPersistenceAdapterTest {
  private val repository: SpringDataWalletRepository = mock(SpringDataWalletRepository::class.java)
  private val adapter = WalletPersistenceAdapter(repository)

  private val mockWalletId = WalletId.generate()
  private val mockOwnerId = OwnerId.generate()
  private val mockBankAccountId = BankAccountId.generate()

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

  @Test
  fun `findById should return Left(WalletNotFoundException) when entity does not exist`() {
    // Given
    given(repository.findById(mockWalletId.value)).willReturn(Mono.empty())

    // When
    val actualResult = adapter.findById(mockWalletId)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should not find wallet")
        assertEquals(WalletNotFoundException::class.java, error::class.java)
      }.verifyComplete()
  }

  @Test
  fun `save should return Right(Wallet) when successful`() {
    // Given
    val wallet = createWallet()
    val entity = createWalletEntity(wallet.id.value)

    given(repository.save(any())).willReturn(Mono.just(entity))

    // When
    val actualResult = adapter.save(wallet)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val savedWallet = result.expectRight("Should save wallet")
        assertEquals(wallet.id, savedWallet.id)
      }.verifyComplete()
  }

  @Test
  fun `save should return Left(WalletException) when repository unexpected error`() {
    // Given
    val wallet = createWallet()
    given(
      repository.save(any(WalletEntity::class.java)),
    ).willReturn(Mono.error(RuntimeException("DB Error")))

    // When
    val actualResult = adapter.save(wallet)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail to save")
        assertEquals(WalletException::class.java, error::class.java)
        assertEquals("Unexpected System Error", error.message)
      }.verifyComplete()
  }

  @Test
  fun `save should return Left(WalletAlreadyExistsException) when repository fails`() {
    // Given
    val wallet = createWallet()
    val errorMessage = "Wallet with ID [${wallet.id.value}] already existed"
    given(
      repository.save(any(WalletEntity::class.java)),
    ).willReturn(Mono.error(DuplicateKeyException(errorMessage)))

    // When
    val actualResult = adapter.save(wallet)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail to save")
        assertInstanceOf(WalletAlreadyExistsException::class.java, error)
        error.message?.also { assertContains(it, wallet.id.value.toString()) }
      }.verifyComplete()
  }

  @Test
  fun `save should return Left(WalletConcurrencyException) on optimistic lock failure`() {
    // Given
    val wallet = createWallet()
    given(
      repository.save(any(WalletEntity::class.java)),
    ).willReturn(Mono.error(OptimisticLockingFailureException("Version mismatch")))

    // When
    val actualResult = adapter.save(wallet)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail to save")
        assertInstanceOf(WalletConcurrencyException::class.java, error)
        assertEquals(
          "Wallet with ID [${wallet.id.value}] has been modified by another transaction",
          error.message,
        )
        assertInstanceOf(OptimisticLockingFailureException::class.java, error.cause)
      }.verifyComplete()
  }

  private fun createWalletEntity(id: UUID) =
    WalletEntity(
      _id = id,
      name = "Test Wallet",
      balanceAmount = BigDecimal.TEN,
      balanceCurrency = Currency.USD.name,
      linkedBankAccountId = mockBankAccountId.value,
      ownerId = mockOwnerId.value,
      status = "ACTIVE",
      version = 1L,
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
    )

  private fun createWallet() =
    Wallet(
      id = mockWalletId,
      name = "Test Wallet",
      balance = Money(BigDecimal.TEN, Currency.USD),
      linkedBankAccountId = mockBankAccountId,
      ownerId = mockOwnerId,
      status = WalletStatus.ACTIVE,
      version = 1L,
    )
}
