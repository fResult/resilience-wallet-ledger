package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.IdGenerator
import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import io.vavr.control.Either
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class WalletServiceTest {
  @Mock
  private lateinit var walletRepository: WalletRepository

  @Mock
  private lateinit var idGenerator: IdGenerator

  private lateinit var walletApplicationService: WalletApplicationService

  @BeforeEach
  fun setUp() {
    walletApplicationService = WalletApplicationService(walletRepository, idGenerator)
  }

  @Nested
  inner class CreateWallet {
    private val expectedOwnerId = OwnerId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
    private val expectedWalletName = "My Wallet"
    private val expectedBalance = Money(BigDecimal.ZERO, Currency.JPY)
    private val expectedVersion = 1L

    @Test
    fun `should return Right(Wallet) when persistence is successful`() {
      // Given
      mockSuccessfulPersistence(expectedVersion)

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
        }.verifyComplete()
    }

    @Test
    fun `should return Left(WalletException) should return failure`() {
      // Given
      val expectedErrorMessage = "Persistence failed"
      mockFailurePersistence(WalletException(expectedErrorMessage))

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
    @Test
    fun `should return Right(Wallet) when deposit is successful`() {
      // Given
      val walletId = WalletId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
      val ownerId = OwnerId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
      val initialBalance = thb(1500)
      val initialVersion = 3L
      val amountToDeposit = thb(500)
      val expectedBalance = initialBalance + amountToDeposit
      val expectedVersion = 4L
      val existingWallet =
        Wallet
          .create(
            walletId,
            ownerId,
            "My Wallet",
            initialBalance.currency,
          ).copy(balance = initialBalance, version = initialVersion)

      given(
        walletRepository.findById(existingWallet.id),
      ).willReturn(Mono.just(Either.right(existingWallet)))

      given(walletRepository.save(any<Wallet>())).willAnswer { invocation ->
        val walletBeingDeposited = invocation.getArgument<Wallet>(0)
        val depositedWallet = walletBeingDeposited.copy(version = walletBeingDeposited.version + 1)
        Mono.just(Either.right<WalletException, Wallet>(depositedWallet))
      }

      // When
      val actualResult = walletApplicationService.deposit(existingWallet.id, amountToDeposit)

      // Then
      StepVerifier
        .create(actualResult)
        .assertNext { result ->
          val actualResult = result.expectRight("Should deposit successfully")

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
    given(walletRepository.save(any<Wallet>()))
      .willAnswer { invocation ->
        val walletToCreated = invocation.getArgument<Wallet>(0)
        val createdWallet = walletToCreated.copy(version = version)
        Mono.just(Either.right<WalletException, Wallet>(createdWallet))
      }
  }

  private fun mockFailurePersistence(cause: WalletException) {
    given(walletRepository.save(any<Wallet>()))
      .willReturn(Mono.just(Either.left(cause)))
  }

  private fun thb(amount: Int): Money = Money(amount.toBigDecimal(), Currency.THB)
}
