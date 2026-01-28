package com.fResult.resilienceWalletLedger.wallet.internal.domain.event

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import java.time.Instant
import java.util.UUID

data class MoneyDepositFailed(
  override val eventId: UUID,
  val walletId: WalletId,
  val amount: Money,
  val refTransactionId: String,
  val reason: String,
  override val occurredOn: Instant,
) : WalletEvent
