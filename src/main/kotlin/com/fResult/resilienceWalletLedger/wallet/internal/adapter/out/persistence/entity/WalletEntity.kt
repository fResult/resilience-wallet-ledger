package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("wallets")
data class WalletEntity(
  @Id
  @Column("id")
  private val _id: UUID,
  val name: String,
  val balanceAmount: BigDecimal,
  val balanceCurrency: String,
  val linkedBankAccountId: UUID?,
  val ownerId: UUID,
  val status: String,
  @Version val version: Long? = null,
  @CreatedDate
  var createdAt: Instant? = null,
  @LastModifiedDate
  var updatedAt: Instant? = null,
) : Persistable<UUID> {
  override fun getId(): UUID = _id

  @Transient
  override fun isNew(): Boolean = version == null
}
