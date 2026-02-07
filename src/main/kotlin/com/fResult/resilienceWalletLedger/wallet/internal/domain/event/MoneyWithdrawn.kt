package com.fResult.resilienceWalletLedger.wallet.internal.domain.event

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import java.time.Instant
import java.util.UUID

data class MoneyWithdrawn(
  override val eventId: UUID,
  val amount: Money,
  /** Post-transaction balance */
  val currentBalance: Money,
  val refTransactionId: String,
  override val occurredOn: Instant,
) : WalletEvent
