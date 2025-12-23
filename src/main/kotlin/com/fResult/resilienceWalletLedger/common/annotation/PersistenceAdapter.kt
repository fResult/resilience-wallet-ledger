package com.fResult.resilienceWalletLedger.common.annotation

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class PersistenceAdapter(
  @get:AliasFor(annotation = Component::class)
  val value: String = "",
)
