package com.fResult.resilienceWalletLedger.common.extension

fun <A, B, C, D> Mono<Pair<A, B>>.bimapPair(
  mapFirst: (A) -> C,
  mapSecond: (B) -> D,
) = this.map { it.bimap(mapFirst, mapSecond) }
