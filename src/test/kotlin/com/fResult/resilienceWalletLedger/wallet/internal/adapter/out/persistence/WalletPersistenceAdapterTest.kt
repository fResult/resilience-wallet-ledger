package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.Clock
import com.fResult.resilienceWalletLedger.common.exception.InvariantViolationException
import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletOutboxEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataOutboxRepository
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
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
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.mockito.kotlin.any
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.ObjectMapper

class WalletPersistenceAdapterTest {
  private val walletRepository = mock(SpringDataWalletRepository::class.java)
  private val outboxRepository = mock(SpringDataOutboxRepository::class.java)
  private val mapper = mock(ObjectMapper::class.java)
  private var clock = mock(Clock::class.java)

  private val adapter = WalletPersistenceAdapter(walletRepository, outboxRepository, mapper, clock)

  private val mockWalletId = WalletId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
  private val mockOwnerId = OwnerId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
  private val mockBankAccountId =
    BankAccountId(UUID.fromString("00000000-0000-0000-0000-000000000003"))

  @Test
  fun `findById should return Right(Wallet) when entity exists`() {
    // Given
    val expectedResult = createWalletEntity(mockWalletId.value)
    given(walletRepository.findById(mockWalletId.value)).willReturn(Mono.just(expectedResult))

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
    given(walletRepository.findById(mockWalletId.value)).willReturn(Mono.empty())

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
  fun `findById should fail with InvariantViolationException on corrupted data`() {
    // Given
    @Suppress("CAST_NEVER_SUCCEEDS")
    val corruptedEntity = createWalletEntity(mockWalletId.value).copy(_id = null as UUID)
    given(walletRepository.findById(mockWalletId.value)).willReturn(Mono.just(corruptedEntity))

    // When
    val actualResult = adapter.findById(mockWalletId)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should catch mapping error")
        assertInstanceOf(WalletException::class.java, error)
        assertEquals("Unexpected System Error", error.message)
        assertInstanceOf(InvariantViolationException::class.java, error.cause)
        assertEquals(
          "CRITICAL: Found WalletEntity with null ID inside DB! This is a bug.",
          error.cause?.message,
        )
      }.verifyComplete()
  }

  @Test
  fun `save should return Right(Wallet) when successful`() {
    // Given
    val wallet = createWallet()
    val entity = createWalletEntity(wallet.id.value)
    val events = emptyList<WalletEvent>()
    val fixedTime = Instant.parse("2026-01-15T10:00:00Z")

    given(clock.now()).willReturn(fixedTime)
    given(walletRepository.save(any<WalletEntity>())).willReturn(Mono.just(entity))
    given(outboxRepository.saveAll(any<List<WalletOutboxEntity>>())).willReturn(Flux.empty())

    // When
    val response = adapter.save(wallet to events)

    // Then
    StepVerifier
      .create(response)
      .assertNext { result ->
        val (savedWallet, savedEvents) = result.expectRight("Should save wallet")
        assertEquals(wallet.id, savedWallet.id)
      }.verifyComplete()
  }

  @Test
  fun `findById should return Left when data mapping fails (Invalid Enum)`() {
    // Given
    val invalidEnumEntity = createWalletEntity(mockWalletId.value).copy(balanceCurrency = "BITCOIN")
    given(walletRepository.findById(mockWalletId.value)).willReturn(Mono.just(invalidEnumEntity))

    // When
    val actualResult = adapter.findById(mockWalletId)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should handle enum mapping error")
        assertInstanceOf(WalletException::class.java, error)
        assertEquals("Unexpected System Error", error.message)
        assertInstanceOf(IllegalArgumentException::class.java, error.cause)
      }.verifyComplete()
  }

  @Test
  fun `save should return Left(WalletException) when repository unexpected error`() {
    // Given
    val wallet = createWallet()
    val events = emptyList<WalletEvent>()
    given(
      walletRepository.save(any<WalletEntity>()),
    ).willReturn(Mono.error(RuntimeException("DB Error")))

    // When
    val actualResult = adapter.save(wallet to events)

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
    val events = emptyList<WalletEvent>()
    val errorMessage = "Wallet with ID [${wallet.id.value}] already existed"
    given(
      walletRepository.save(any<WalletEntity>()),
    ).willReturn(Mono.error(DuplicateKeyException(errorMessage)))

    // When
    val actualResult = adapter.save(wallet to events)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail to save")
        assertInstanceOf(WalletAlreadyExistsException::class.java, error)
        error.message?.also { assertContains(it, wallet.id.value.toString()) }
        assertInstanceOf(DuplicateKeyException::class.java, error.cause)
      }.verifyComplete()
  }

  @Test
  fun `save should return Left(WalletConcurrencyException) on optimistic lock failure`() {
    // Given
    val wallet = createWallet()
    val events = emptyList<WalletEvent>()
    given(
      walletRepository.save(any<WalletEntity>()),
    ).willReturn(Mono.error(OptimisticLockingFailureException("Version mismatch")))

    // When
    val actualResult = adapter.save(wallet to events)

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

  @Test
  fun `save should return Left(WalletException) on data violation failure`() {
    // Given
    val dbErrorMessage = "DB Error"
    val expectedErrorMessage = "Data Integrity Violation: $dbErrorMessage"
    val wallet = createWallet()
    val events = emptyList<WalletEvent>()
    given(
      walletRepository.save(any<WalletEntity>()),
    ).willReturn(Mono.error(DataIntegrityViolationException(dbErrorMessage)))

    // When
    val actualResult = adapter.save(wallet to events)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail to save")
        assertInstanceOf(WalletException::class.java, error)
        assertEquals(expectedErrorMessage, error.message)
        assertInstanceOf(DataIntegrityViolationException::class.java, error.cause)
      }.verifyComplete()
  }

  @Test
  fun `save should handle empty Mono from repository (Safety Check)`() {
    // Given
    val wallet = createWallet()
    val events = emptyList<WalletEvent>()
    given(walletRepository.save(any<WalletEntity>())).willReturn(Mono.empty())

    // When
    val actualResult = adapter.save(wallet to events)

    // Then
    StepVerifier
      .create(actualResult)
      .assertNext { result ->
        val error = result.expectLeft("Should fail on empty result")
        assertInstanceOf(WalletException::class.java, error)
        assertEquals("Unexpected System Error", error.message)
        assertEquals("Save returned empty", error.cause?.message)
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
      createdAt = Instant.now(),
      version = 1L,
    )
}
