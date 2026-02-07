package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.Clock
import com.fResult.resilienceWalletLedger.common.IdGenerator
import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.command.CreateWalletCommand
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class WalletApplicationServiceTest {
  @Mock
  private lateinit var walletRepository: WalletRepository

  @Mock
  private lateinit var idGenerator: IdGenerator

  @Mock
  private lateinit var clock: Clock

  @InjectMocks
  private lateinit var walletApplicationService: WalletApplicationService

  @Nested
  inner class CreateWallet {
    private val expectedOwnerId = OwnerId(UUID.fromString("019c088e-6a14-7d23-837c-ca3b05033a0a"))
    private val expectedWalletName = "My Wallet"
    private val expectedBalance = Money(BigDecimal.ZERO, Currency.JPY)
    private val expectedVersion = 1L
    private val fixedTime = Instant.parse("2026-01-15T10:00:00Z")
    private val fixedEventUuid = UUID.fromString("019c088a-f22e-7009-9e51-9694ea8cbfa8")

    @Test
    fun `should return Right(Wallet) when persistence is successful`() {
      // Given
      mockSuccessfulPersistence(expectedVersion)
      // 🪄 Magic: Freeze time and UUID
      given(clock.now()).willReturn(fixedTime)
      given(idGenerator.generate()).willReturn(fixedEventUuid)

      // When
      val actualResult =
        walletApplicationService.createWallet(
          expectedWalletName,
          expectedOwnerId,
          expectedBalance.currency,
        )

      // Then
      StepVerifier
        .create(actualResult)
        .assertNext { result ->
          val createdWallet = result.expectRight("Wallet created successfully")

          assertEquals(expectedOwnerId, createdWallet.ownerId)
          assertEquals(expectedWalletName, createdWallet.name)
          assertEquals(expectedBalance, createdWallet.balance)
          assertEquals(expectedVersion, createdWallet.version)
          assertEquals(fixedTime, createdWallet.createdAt)
        }.verifyComplete()
    }

    @Test
    fun `should return Left(WalletException) should return failure`() {
      // Given
      val expectedErrorMessage = "Persistence failed"
      mockFailurePersistence(WalletException(expectedErrorMessage))
      given(clock.now()).willReturn(fixedTime)
      given(idGenerator.generate()).willReturn(fixedEventUuid)

      // When
      val actualResult =
        walletApplicationService.createWallet(
          expectedWalletName,
          expectedOwnerId,
          expectedBalance.currency,
        )

      // Then
      StepVerifier
        .create(actualResult)
        .assertNext { result ->
          val error = result.expectLeft("Wallet creation failed")
          assertEquals(error.message, expectedErrorMessage)
        }
    }
  }

  @Nested
  inner class Deposit {
    val walletUuid: UUID = UUID.fromString("019c0887-990e-7c44-842e-e6cb2f53d5ac")
    val ownerUuid: UUID = UUID.fromString("019c088e-6a14-7d23-837c-ca3b05033a0a")
    val fixedEventUuid: UUID = UUID.fromString("019c088a-f22e-7009-9e51-9694ea8cbfa8")
    val fixedIdempotencyKey: UUID = UUID.fromString("019c088d-9968-7e1f-9a93-01a0b5d02d98")

    @Test
    fun `should return Right(Wallet) when deposit is successful`() {
      // Given
      val fixedTime = Instant.parse("2026-01-15T10:00:00Z")
      val walletId = WalletId(walletUuid)
      val ownerId = OwnerId(ownerUuid)
      val initialBalance = thb(1500)
      val initialVersion = 3L
      val amountToDeposit = thb(500)
      val expectedBalance = initialBalance + amountToDeposit
      val expectedVersion = 4L
      val command =
        CreateWalletCommand(walletId, ownerId, "My Wallet", Currency.THB, fixedEventUuid, fixedTime)
      val (newWallet, events) = Wallet.create(command)
      val existingWallet = newWallet.copy(balance = initialBalance, version = initialVersion)

      given(
        walletRepository.findById(existingWallet.id),
      ).willReturn(Mono.just(Either.right(existingWallet)))

      given(idGenerator.generate()).willReturn(fixedEventUuid)
      given(clock.now()).willReturn(fixedTime)

      given(
        walletRepository.save(any<Pair<Wallet, List<WalletEvent>>>()),
      ).willAnswer { invocation ->
        val (walletBeingDeposited, events) =
          invocation
            .getArgument<Pair<Wallet, List<WalletEvent>>>(
              0,
            )
        val depositedWallet = walletBeingDeposited.copy(version = walletBeingDeposited.version + 1)
        Mono.just(
          Either.right<WalletException, Pair<Wallet, List<WalletEvent>>>(depositedWallet to events),
        )
      }

      // When
      val actualResult =
        walletApplicationService.deposit(
          existingWallet.id,
          amountToDeposit,
          fixedIdempotencyKey.toString(),
        )

      // Then
      StepVerifier
        .create(actualResult)
        .assertNext { response ->
          val actualResult = response.expectRight("Should deposit successfully")

          assertEquals(
            expectedBalance,
            actualResult.balance,
            "Balance should be ${amountToDeposit.amount}",
          )
          assertEquals(expectedVersion, actualResult.version, "Version should increment")
        }.verifyComplete()
    }
  }

  private fun mockSuccessfulPersistence(version: Long = 1L) {
    given(walletRepository.save(any<Pair<Wallet, List<WalletEvent>>>()))
      .willAnswer { invocation ->
        val walletToCreated = invocation.getArgument<Wallet>(0)
        val createdWallet = walletToCreated.copy(version = version)
        Mono.just(Either.right<WalletException, Wallet>(createdWallet))
      }
  }

  private fun mockFailurePersistence(cause: WalletException) {
    given(walletRepository.save(any<Pair<Wallet, List<WalletEvent>>>()))
      .willReturn(Mono.just(Either.left(cause)))
  }

  private fun thb(amount: Int): Money = Money(amount.toBigDecimal(), Currency.THB)
}
