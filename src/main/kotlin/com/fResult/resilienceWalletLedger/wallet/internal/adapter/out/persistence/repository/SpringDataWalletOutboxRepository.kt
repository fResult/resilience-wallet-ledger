package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository

import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletOutboxEntity
import java.util.UUID
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface SpringDataWalletOutboxRepository : R2dbcRepository<WalletOutboxEntity, UUID>
