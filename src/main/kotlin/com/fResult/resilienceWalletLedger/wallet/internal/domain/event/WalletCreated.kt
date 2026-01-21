package com.fResult.resilienceWalletLedger.wallet.internal.domain.event

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.BankAccountId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import java.time.Instant
import java.util.UUID

data class WalletCreated(
  override val eventId: UUID,
  val walletId: WalletId,
  val ownerId: OwnerId,
  val linkedBankAccountId: BankAccountId?,
  val name: String,
  val initialBalance: Money,
  override val occurredOn: Instant,
) : WalletEvent
