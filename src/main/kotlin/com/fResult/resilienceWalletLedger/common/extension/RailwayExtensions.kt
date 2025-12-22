package com.fResult.resilienceWalletLedger.common.extension

import com.fResult.resilienceWalletLedger.common.exception.BusinessRuleViolation
import com.fResult.resilienceWalletLedger.common.exception.ConcurrencyConflict
import com.fResult.resilienceWalletLedger.common.exception.DomainError
import com.fResult.resilienceWalletLedger.common.exception.ResourceNotFound
import com.fResult.resilienceWalletLedger.wallet.internal.domain.exception.WalletException
import io.vavr.control.Either
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono

/**
 * Utilize to transform the System Failure (Exception) to be a Domain Failure (Either.Left)
 * For the Railway-Oriented Programming Workflow
 */
fun <T : Any> Mono<T>.toEither(ifEmpty: () -> WalletException): Mono<Either<WalletException, T>> =
  this
    .map { Either.right<WalletException, T>(it) }
    .defaultIfEmpty(Either.left(ifEmpty()))
    .onErrorResume { ex ->
      Mono.just(Either.left(WalletException("Unexpected System Error", ex)))
    }

private fun DomainError.resolveHttpStatus(): HttpStatus =
  when (this) {
    is ResourceNotFound -> HttpStatus.NOT_FOUND
    is BusinessRuleViolation -> HttpStatus.UNPROCESSABLE_ENTITY
    is ConcurrencyConflict -> HttpStatus.CONFLICT
    else -> HttpStatus.INTERNAL_SERVER_ERROR
  }
