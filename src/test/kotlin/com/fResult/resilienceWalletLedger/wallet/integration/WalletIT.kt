package com.fResult.resilienceWalletLedger.wallet.integration

import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletOutboxRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.service.WalletApplicationService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletIT {
  companion object {
    @Container
    val postgres =
      PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine3.23"))
        .apply {
          withDatabaseName("wallet_db")
          withUsername("testuser")
          withPassword("testpassword")
        }

    @JvmStatic
    @DynamicPropertySource
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
      registry.apply {
        add("spring.datasource.url") {
          "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/wallet_db"
        }
        add("spring.datasource.username", postgres::getUsername)
        add("spring.datasource.password", postgres::getPassword)

        // Override Flyway configs, since flyway uses JDBC
        add("spring.flyway.url", postgres::getJdbcUrl)
        add("spring.flyway.user", postgres::getUsername)
        add("spring.flyway.password", postgres::getPassword)
      }
    }
  }

  @Autowired
  lateinit var walletRepository: WalletRepository

  @Autowired
  lateinit var walletApplicationService: WalletApplicationService

  @Autowired
  lateinit var outboxRepository: SpringDataWalletOutboxRepository

  @Test
  fun `context load`() {
    // TODO("Not yet implemented")
  }
}
