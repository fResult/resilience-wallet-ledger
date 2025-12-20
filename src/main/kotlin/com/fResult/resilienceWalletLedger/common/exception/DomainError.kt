package com.fResult.resilienceWalletLedger.common.exception

sealed interface DomainError {
  val message: String?
}
