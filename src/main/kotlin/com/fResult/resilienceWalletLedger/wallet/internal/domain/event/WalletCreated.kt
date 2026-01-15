package com.fResult.resilienceWalletLedger.wallet.internal.domain.event

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import java.time.Instant
import java.util.UUID

data class WalletCreated(
  val eventId: UUID,
  val walletId: UUID,
  val ownerId: UUID,
  val linkedBankAccountId: UUID?,
  val name: String,
  val initialBalance: Money,
  override val occurredOn: Instant,
) : WalletEvent
