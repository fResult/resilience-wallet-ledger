package com.fResult.resilienceWalletLedger.common

import com.fasterxml.uuid.Generators
import java.util.UUID

object IdGenerator {
  private val generator = Generators.timeBasedEpochGenerator()

  fun generate(): UUID = generator.generate()
}
