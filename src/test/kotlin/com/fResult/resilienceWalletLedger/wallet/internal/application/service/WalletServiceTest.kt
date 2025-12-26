package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.common.fixtures.expectLeft
import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import io.vavr.control.Either
import java.math.BigDecimal
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
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
  private val expectedOwnerId = OwnerId.generate()
  private val expectedWalletName = "My Wallet"
  private val expectedBalance = Money(BigDecimal.ZERO, Currency.JPY)
  private val expectedVersion = 1L

  @Mock
  private lateinit var walletRepository: WalletRepository

  private lateinit var walletService: WalletService

  @BeforeEach
  fun setUp() {
    walletService = WalletService(walletRepository)
  }

  @Test
  fun `createWallet should return Right(Wallet) when persistence is successful`() {
    // Given
    mockSuccessfulPersistence(expectedVersion)

    // When
    val actualResult =
      walletService.createWallet(
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
  fun `createWallet should return Left(WalletException) should return failure`() {
    // Given
    val expectedErrorMessage = "Persistence failed"
    given(
      walletRepository.save(any<Wallet>()),
    ).willReturn(Mono.just(Either.left(WalletException(expectedErrorMessage))))

    // When
    val actualResult =
      walletService.createWallet(
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

  private fun mockSuccessfulPersistence(version: Long = 1L) {
    given(walletRepository.save(any<Wallet>()))
      .willAnswer { invocation ->
        val walletToCreated = invocation.getArgument<Wallet>(0)
        val createdWallet = walletToCreated.copy(version = version)
        Mono.just(Either.right<WalletException, Wallet>(createdWallet))
      }
  }
}
