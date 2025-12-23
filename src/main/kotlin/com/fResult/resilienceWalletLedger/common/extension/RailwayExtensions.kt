package com.fResult.resilienceWalletLedger.common.extension

import com.fResult.resilienceWalletLedger.common.exception.BusinessRuleViolation
import com.fResult.resilienceWalletLedger.common.exception.ConcurrencyConflict
import com.fResult.resilienceWalletLedger.common.exception.DomainError
import com.fResult.resilienceWalletLedger.common.exception.ResourceNotFound
import io.vavr.control.Either
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono

/**
 * 🔵 For Queries (findById, findOne): High Strictness
 * Transforms Mono<T> into Either.Right<T>.
 * - If Empty: Maps to Either.Left via [onEmpty] (e.g., WalletNotFound).
 * - If Error: Maps to Either.Left via [onError] (e.g., DatabaseError -> SystemError).
 */
fun <T : Any, E> Mono<T>.queryToEither(
  onError: (Throwable) -> E,
  onEmpty: () -> E,
): Mono<Either<E, T>> =
  this
    .map { Either.right<E, T>(it) }
    .defaultIfEmpty(Either.left(onEmpty()))
    .onErrorResume { ex ->
      Mono.just(Either.left(onError(ex)))
    }

/**
 * 🔴 For Commands (save, update, delete): Low Strictness
 * Transforms Mono<T> into Either.Right<T>.
 * - If Empty: Maps to Either.Right (Implies Success/Void completion).
 * - If Error: Maps to Either.Left via [onError].
 * * Note: If T is Void, it naturally returns Right(Unit).
 */
fun <T : Any, E> Mono<T>.commandToEither(onError: (Throwable) -> E): Mono<Either<E, T>> =
  this
    .map { Either.right<E, T>(it) }
    .defaultIfEmpty(Either.right(null))
    .onErrorResume { ex ->
      Mono.just(Either.left(onError(ex)))
    }

private fun DomainError.resolveHttpStatus(): HttpStatus =
  when (this) {
    is ResourceNotFound -> HttpStatus.NOT_FOUND
    is BusinessRuleViolation -> HttpStatus.UNPROCESSABLE_ENTITY
    is ConcurrencyConflict -> HttpStatus.CONFLICT
    else -> HttpStatus.INTERNAL_SERVER_ERROR
  }
