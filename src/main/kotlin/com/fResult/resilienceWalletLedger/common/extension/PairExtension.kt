package com.fResult.resilienceWalletLedger.common.extension

fun <A, B, C, D> Pair<A, B>.bimap(
  mapFirst: (A) -> C,
  mapSecond: (B) -> D,
) = mapFirst(first) to mapSecond(second)
