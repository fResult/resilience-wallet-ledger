plugins {
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.spring") version "2.2.21"
  id("org.springframework.boot") version "4.0.1"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.diffplug.spotless") version "8.1.0"
}

group = "com.fResult"
version = "0.0.1"
description = "Practice Real-World Project for Distributed System using Kotlin and Spring Boot"

java { toolchain { languageVersion = JavaLanguageVersion.of(24) } }

repositories { mavenCentral() }

val vavrVersion = "0.11.0"
val uuidGeneratorVersion = "5.2.0"
val mockitoKotlinVersion = "6.1.0"
val testcontainersVersion = "2.0.3"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("tools.jackson.module:jackson-module-kotlin")
  // Upgrade to Vavr 1.0.0 if available
  implementation("io.vavr:vavr:$vavrVersion")
  implementation("com.fasterxml.uuid:java-uuid-generator:$uuidGeneratorVersion")
  implementation("org.postgresql:r2dbc-postgresql")

  runtimeOnly("org.postgresql:postgresql")

  testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
  testImplementation("org.springframework.boot:spring-boot-starter-r2dbc-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testImplementation("org.testcontainers:testcontainers-postgresql")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
  }
}

spotless {
  kotlin {
    ktlint()

    target("**/*.kt")
    targetExclude("**/build/**")

    trimTrailingWhitespace()
    leadingTabsToSpaces()
    endWithNewline()
  }

  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.register("installGitHooks") {
  description = "Installs git hooks to run spotless check before commit"
  group = "help"

  doLast {
    val preCommitFile = file(".git/hooks/pre-commit")
    val script =
      $$"""
      #!/bin/sh
      
      STAGED_FILES=$(git diff --name-only --cached --diff-filter=ACMR | grep -E "\.kt\$|\.kts\$")
      echo "Staged Files:\n $STAGED_FILES"

      if [ -z "$STAGED_FILES" ]; then
        exit 0
      fi

      echo "🔍 Running Spotless Check..."

      ./gradlew spotlessCheck
      RESULT=$?

      if [ $RESULT -ne 0 ]; then
        echo "❌ Spotless check failed!"
        echo "👉 Please run ./gradlew spotlessApply and re-stage manually"
        exit 1
      fi

      echo "✅ Spotless check passed"
      exit 0
      """.trimIndent()

    preCommitFile.writeText(script)
    preCommitFile.setExecutable(true)
    println("Git hooks installed successfully!")
  }

  tasks.getByPath("build").dependsOn("installGitHooks")
}
