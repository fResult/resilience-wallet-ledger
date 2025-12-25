package com.fResult.resilienceWalletLedger.common.error

sealed interface DomainError {
  val message: String?
}
