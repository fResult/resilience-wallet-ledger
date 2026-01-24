package com.fResult.resilienceWalletLedger.common.extension

import io.vavr.control.Either
import reactor.core.publisher.Mono

fun <A, B, C, D> Mono<Pair<A, B>>.bimapPair(
  mapFirst: (A) -> C,
  mapSecond: (B) -> D,
): Mono<Pair<C, D>> = this.map { it.bimap(mapFirst, mapSecond) }
