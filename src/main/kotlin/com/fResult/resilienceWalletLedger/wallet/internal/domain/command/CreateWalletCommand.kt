package com.fResult.resilienceWalletLedger.wallet.internal.domain.command

import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import java.time.Instant
import java.util.UUID

data class CreateWalletCommand(
  val walletId: WalletId,
  val ownerId: OwnerId,
  val name: String,
  val currency: Currency,
  override val eventId: UUID,
  override val occurredOn: Instant,
) : WalletCommand
