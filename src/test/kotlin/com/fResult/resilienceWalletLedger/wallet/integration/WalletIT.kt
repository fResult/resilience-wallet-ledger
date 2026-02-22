package com.fResult.resilienceWalletLedger.wallet.integration

import com.fResult.resilienceWalletLedger.common.fixtures.expectRight
import com.fResult.resilienceWalletLedger.wallet.internal.adapter.out.persistence.repository.SpringDataWalletOutboxRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.port.out.WalletRepository
import com.fResult.resilienceWalletLedger.wallet.internal.application.service.WalletApplicationService
import com.fResult.resilienceWalletLedger.wallet.internal.domain.event.WalletCreated
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Currency
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Money
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.OwnerId
import com.fResult.resilienceWalletLedger.wallet.internal.domain.model.Wallet
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {
  @Bean
  @ServiceConnection
  fun postgresContainer(): PostgreSQLContainer =
    PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine3.23"))
      .withDatabaseName("wallet_db")
      .withUsername("testuser")
      .withPassword("testpassword")
}

@Import(TestcontainersConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletIT {
  companion object {
//    @Container
//    @JvmStatic
//    @ServiceConnection
//    val postgres =
//      PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine3.23"))
//        .apply {
//          withDatabaseName("wallet_db")
//          withUsername("testuser")
//          withPassword("testpassword")
// //          start()
//        }

//    @JvmStatic
//    @DynamicPropertySource
//    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
//      Flyway.configure()
//        .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
//        .locations("classpath:db/migration")
//        .load()
//        .migrate()
//
//      registry.apply {
//        add("spring.r2dbc.url") {
//          "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"
//        }
// //        add("spring.datasource.username", postgres::getUsername)
// //        add("spring.datasource.password", postgres::getPassword)
//        add("spring.r2dbc.username", postgres::getUsername)
//        add("spring.r2dbc.password", postgres::getPassword)
//
//        // Override Flyway configs, since flyway uses JDBC
// //        add("spring.flyway.url", postgres::getJdbcUrl)
// //        add("spring.flyway.user", postgres::getUsername)
// //        add("spring.flyway.password", postgres::getPassword)
// //        add("spring.flyway.driver-class-name") { "org.postgresql.Driver" } // ระบุให้ชัด กันเหนียว
//
// //        add("spring.flyway.enabled") { "true" }
//        add("spring.flyway.enabled") { "false" }
//      }
//    }
  }

  @Autowired
  lateinit var walletApplicationService: WalletApplicationService

  @Autowired
  lateinit var walletRepository: WalletRepository

  @Autowired
  lateinit var outboxRepository: SpringDataWalletOutboxRepository

  @Test
  fun `context load`() {
    // Just to verify that the context loads successfully
  }

  @Test
  fun `should deposit money and persist event to outbox`() {
    // Given
    // Create Wallet
    // - Call service.createWallet()
    // - Block or Chain to get walletId
    val ownerId = OwnerId(UUID.randomUUID())
    val walletName = "My Integration Wallet"
    val currency = Currency.USD
    val depositAmount = Money(BigDecimal.TEN, currency)
    val refTransactionId = "txn-12345"

    val createdWalletIdMono =
      walletApplicationService
        .createWallet(walletName, ownerId, currency)
        .map { createdWalletResult ->
          createdWalletResult.expectRight(("Wallet creation should success"))
        }
//        .map { it.expectRight(("Wallet creation should failed")) }
        .map(Wallet::id)

    // When
    // Deposit Money
    // - Call service.deposit(walletId, amount, refId)
    // - Use StepVerifier to verify that it returns Success (Either.Right)
    val scenario =
      createdWalletIdMono.flatMap { walletId ->
        walletApplicationService
          .deposit(walletId, depositAmount, refTransactionId)
          .map { result ->
            val depositedWallet = result.expectRight("Deposit should success")
            depositedWallet
          }
      }

    // Then
    // Verify Side Effect
    // - Query Wallet from DB -> Check balance
    // - Query Outbox from DB -> Check that MoneyDeposited Event is existed
    StepVerifier
      .create(scenario)
      .assertNext { updatedWallet ->
        assertEquals(depositAmount, updatedWallet.balance)
        assertEquals(1, updatedWallet.version)

        StepVerifier
          .create(walletRepository.findById(updatedWallet.id))
          .assertNext { walletResult ->
            val walletFromDb = walletResult.expectRight("Should find wallet in DB")
            assertEquals(depositAmount, walletFromDb.balance)
          }.verifyComplete()

        StepVerifier
          .create(outboxRepository.findAll())
          .assertNext { outboxEvent ->
            assertEquals(WalletCreated::class.simpleName, outboxEvent.eventType)
            assertEquals(updatedWallet.id.value, outboxEvent.walletId)
            assertTrue(outboxEvent.payload.asString().contains("100.00"))
          }.thenCancel()
          .verify()
      }.verifyComplete()
  }
}
