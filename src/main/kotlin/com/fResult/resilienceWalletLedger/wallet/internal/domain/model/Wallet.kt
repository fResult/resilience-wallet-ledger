package com.fResult.resilienceWalletLedger.wallet.internal.domain.model

import java.util.UUID

data class Wallet(
  val id: UUID,
  val name: String,
  val balance: Money,
  val linkedBankAccountId: UUID,
  val ownerId: UUID,
  val status: WalletStatus,
  val version: Long = 0,
)
