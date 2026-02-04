package com.fResult.resilienceWalletLedger.wallet.api

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class WalletHandler(
  walletService: WalletService,
) {
  companion object {
    const val NOT_YET_IMPLEMENTED = "Not yet implemented"
  }

  fun createWallet(): ServerResponse {
    TODO(NOT_YET_IMPLEMENTED)
  }

  fun deposit(idempotencyKey: String): ServerResponse {
    TODO(NOT_YET_IMPLEMENTED)
  }

  fun withdraw(idempotencyKey: String): ServerResponse {
    TODO(NOT_YET_IMPLEMENTED)
  }
}
