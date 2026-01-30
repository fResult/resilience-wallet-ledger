package com.fResult.resilienceWalletLedger.common.extension

import io.vavr.control.Either
import reactor.core.publisher.Mono

// mapRight
fun <L, R, T> Mono<Either<L, R>>.toEitherRight(mapper: (R) -> Either<L, T>): Mono<Either<L, T>> =
  this.map { result -> result.flatMap(mapper) }

fun <L, R, T> Mono<Either<L, R>>.flatMapRight(
  mapper: (R) -> Mono<Either<L, T>>,
): Mono<Either<L, T>> =
  this.flatMap { result ->
    result.fold({ error -> Mono.just(Either.left(error)) }, mapper)
  }

fun <A, B, C, D> Mono<Pair<A, B>>.bimapPair(
  mapFirst: (A) -> C,
  mapSecond: (B) -> D,
): Mono<Pair<C, D>> = this.map { it.bimap(mapFirst, mapSecond) }
