package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.annotation.PersistenceAdapter
import com.fResult.resilienceWalletLedger.common.exception.InvariantViolationException
import com.fResult.resilienceWalletLedger.common.extension.commandToEither
import com.fResult.resilienceWalletLedger.common.extension.queryToEither
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletAlreadyExistsException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletConcurrencyException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletNotFoundException
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.BankAccountId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletStatus
import io.vavr.control.Either
import java.time.Instant
import java.util.UUID
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import reactor.core.publisher.Mono

@PersistenceAdapter
class WalletPersistenceAdapter(
  private val repository: SpringDataWalletRepository,
) : WalletRepository {
  override fun findById(id: WalletId): Mono<Either<WalletException, Wallet>> =
    repository
      .findById(id.value)
      .map(::toDomain)
      .queryToEither(translatePersistenceError(id.value)) {
        WalletNotFoundException("Wallet with ID ${id.value} not found")
      }

  override fun save(wallet: Wallet): Mono<Either<WalletException, Wallet>> =
    wallet
      .let(::toEntity)
      .let(repository::save)
      .map(::toDomain)
      .commandToEither(translatePersistenceError(wallet.id.value))

  private fun toDomain(entity: WalletEntity): Wallet =
    Wallet(
      id =
        WalletId(
          entity.id
            ?: throw InvariantViolationException(
              "CRITICAL: Found WalletEntity with null ID inside DB! This is a bug.",
            ),
        ),
      name = entity.name,
      balance =
        Money(
          amount = entity.balanceAmount,
          currency = Currency.valueOf(entity.balanceCurrency),
        ),
      linkedBankAccountId = entity.linkedBankAccountId?.let(::BankAccountId),
      ownerId = OwnerId(entity.ownerId),
      status = WalletStatus.valueOf(entity.status),
      version = entity.version ?: 0L,
    )

  private fun toEntity(domain: Wallet): WalletEntity =
    WalletEntity(
      _id = domain.id.value,
      name = domain.name,
      balanceAmount = domain.balance.amount,
      balanceCurrency = domain.balance.currency.name,
      linkedBankAccountId = domain.linkedBankAccountId?.value,
      ownerId = domain.ownerId.value,
      status = domain.status.name,
      // version is `null` for new entity (Optimistic Lock)
      version = if (domain.version == 0L) null else domain.version,
      /**
       * FIXME: If update, `createdAt will be always overridden as Now
       */
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
    )

  private fun translatePersistenceError(id: UUID): (Throwable) -> WalletException =
    { ex ->
      when (ex) {
        is DuplicateKeyException ->
          WalletAlreadyExistsException("Wallet with ID [$id] already existed", ex)

        is DataIntegrityViolationException ->
          WalletException("Data Integrity Violation: ${ex.message}", ex)

        is OptimisticLockingFailureException ->
          WalletConcurrencyException(
            "Wallet with ID [$id] has been modified by another transaction",
            ex,
          )

        else -> WalletException("Unexpected System Error", ex)
      }
    }
}
