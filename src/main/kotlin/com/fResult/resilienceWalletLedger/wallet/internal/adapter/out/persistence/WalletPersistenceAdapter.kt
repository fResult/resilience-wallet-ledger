package com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence

import com.fResult.resilienceWalletLedger.common.Clock
import com.fResult.resilienceWalletLedger.common.annotation.PersistenceAdapter
import com.fResult.resilienceWalletLedger.common.exception.InvariantViolationException
import com.fResult.resilienceWalletLedger.common.extension.commandToEither
import com.fResult.resilienceWalletLedger.common.extension.queryToEither
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.entity.WalletOutboxEntity
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletOutboxRepository
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyDeposited
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.MoneyWithdrawn
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletCreated
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletEvent
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
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletResult
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletStatus
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.WalletWithEvents
import io.r2dbc.postgresql.codec.Json
import io.vavr.control.Either
import java.time.Instant
import java.util.UUID
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import reactor.core.publisher.Mono
import tools.jackson.databind.ObjectMapper

@PersistenceAdapter
class WalletPersistenceAdapter(
  private val walletRepository: SpringDataWalletRepository,
  private val outboxRepository: SpringDataWalletOutboxRepository,
  private val mapper: ObjectMapper,
  private val clock: Clock,
) : WalletRepository {
  override fun findById(id: WalletId): Mono<Either<WalletException, Wallet>> =
    walletRepository
      .findById(id.value)
      .map(::toDomain)
      .queryToEither(translatePersistenceError(id.value)) {
        WalletNotFoundException("Wallet with ID ${id.value} not found")
      }

  override fun save(data: WalletWithEvents): Mono<WalletResult<WalletWithEvents>> {
    val (wallet, events) = data

    return wallet
      .let(::toEntity)
      .let(walletRepository::save)
      .switchIfEmpty(Mono.defer { Mono.error(WalletException("Save returned empty")) })
      .zipWhen(recordEvents(events), ::toWalletWithEvents)
      .commandToEither(translatePersistenceError(wallet.id.value))
  }

  private fun toWalletWithEvents(
    savedWallet: WalletEntity,
    recordedEvents: List<WalletOutboxEntity>,
  ): WalletWithEvents = WalletWithEvents(toDomain(savedWallet), recordedEvents.map(::toDomainEvent))

  private fun recordEvents(
    events: List<WalletEvent>,
  ): (WalletEntity) -> Mono<List<WalletOutboxEntity>> =
    { savedWallet ->
      events
        .map(outboxEntryFor(savedWallet))
        .let(outboxRepository::saveAll)
        .collectList()
    }

  private fun toDomain(entity: WalletEntity): Wallet =
    Wallet(
      id =
        WalletId(
          entity.id as UUID?
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
      createdAt = entity.createdAt ?: Instant.now(),
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
      createdAt = domain.createdAt,
      updatedAt = clock.now(),
    )

  private fun outboxEntryFor(walletEntity: WalletEntity): (WalletEvent) -> WalletOutboxEntity =
    { event ->
      WalletOutboxEntity(
        _id = event.eventId,
        walletId = walletEntity.id,
        version = walletEntity.version ?: 0L,
        eventType = event.javaClass.simpleName,
        payload = Json.of(mapper.writeValueAsString(event)),
        occurredOn = event.occurredOn,
      )
    }

  private fun toDomainEvent(outboxEntity: WalletOutboxEntity): WalletEvent =
    when (outboxEntity.eventType) {
      WalletCreated::class.simpleName ->
        mapper.readValue(
          outboxEntity.payload.asString(),
          WalletCreated::class.java,
        )

      MoneyDeposited::class.simpleName ->
        mapper.readValue(
          outboxEntity.payload.asString(),
          MoneyDeposited::class.java,
        )

      MoneyWithdrawn::class.simpleName ->
        mapper.readValue(
          outboxEntity.payload.asString(),
          MoneyWithdrawn::class.java,
        )

      else -> throw WalletException("Unknown event type: ${outboxEntity.eventType}")
    }

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

        else -> WalletException("Unexpected System Error: ${ex.message}", ex)
      }
    }
}
