package com.fResult.resilienceWalletLedger.wallet.internal.application.service

import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class WalletServiceTest {
  @Mock
  private lateinit var walletRepository: WalletRepository

  private lateinit var walletService: WalletService

  @BeforeEach
  fun setUp() {
    walletService = WalletService(walletRepository)
  }
}
