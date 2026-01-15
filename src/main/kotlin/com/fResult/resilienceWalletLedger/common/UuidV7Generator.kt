package com.fResult.resilienceWalletLedger.common

import com.fasterxml.uuid.Generators
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class UuidV7Generator : IdGenerator {
  private val generator = Generators.timeBasedEpochGenerator()

  override fun generate(): UUID = generator.generate()
}
