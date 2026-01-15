package com.fResult.resilienceWalletLedger.wallet.internal.domain.event

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import java.time.Instant
import java.util.UUID

data class MoneyDeposited(
  val eventId: UUID,
  val amount: Money,
  val currentBalance: Money,
  val refTransactionId: String,
  override val occurredOn: Instant,
) : WalletEvent
