package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity

import io.r2dbc.postgresql.codec.Json
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("outbox_wallet_events")
data class WalletOutboxEntity(
  @Id
  @Column("id")
  private val _id: UUID,
  val walletId: UUID, // Domain ID (Changed from aggregate_id)
  val version: Long, // Added for consistency check
  val eventType: String, // event.javaClass.simpleName
  val payload: Json, // JSON String
  val occurredOn: Instant,
  @CreatedDate
  val createdAt: Instant? = null,
) : Persistable<UUID> {
  override fun getId(): UUID = _id

  @Transient
  override fun isNew(): Boolean = createdAt == null
}
