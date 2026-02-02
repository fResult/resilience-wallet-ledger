package com.fResult.resilienceWalletLedger.wallet.internal.domain.command

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import java.time.Instant
import java.util.UUID

data class DepositCommand(
  val amount: Money,
  val refTransactionId: String,
  override val eventId: UUID,
  override val occurredOn: Instant,
) : WalletCommand
