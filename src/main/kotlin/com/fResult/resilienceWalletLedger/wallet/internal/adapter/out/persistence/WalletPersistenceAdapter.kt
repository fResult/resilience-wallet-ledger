package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.annotation.PersistenceAdapter
import com.fResult.resilienceWalletLedger.common.exception.InvariantViolationException
import com.fResult.resilienceWalletLedger.common.extension.toEither
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
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
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono

@PersistenceAdapter
class WalletPersistenceAdapter(
  private val repository: SpringDataWalletRepository,
) : WalletRepository {
  override fun findById(id: WalletId): Mono<Either<WalletException, Wallet>> =
    repository
      .findById(id.value)
      .onErrorResume { ex ->
        when (ex) {
          is DataIntegrityViolationException -> IllegalArgumentException(ex)
          is NoSuchElementException -> WalletNotFoundException(ex.message, ex)
          else -> ex
        }.let(Mono<Wallet>::error)
      }.map(::toDomain)
      .toEither(::WalletNotFoundException) {
        WalletNotFoundException("Wallet with ID ${id.value} not found")
      }

  override fun save(wallet: Wallet): Mono<Either<WalletException, Wallet>> =
    wallet
      .let(::toEntity)
      .let(repository::save)
      .onErrorResume { ex ->
        when (ex) {
          is DuplicateKeyException -> IllegalArgumentException(ex)
          else -> ex
        }.let(Mono<Wallet>::error)
      }.map(::toDomain)
      .toEither(::WalletException) { WalletException("Failed") }

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
}
