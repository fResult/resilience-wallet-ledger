plugins {
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.spring") version "2.2.21"
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.diffplug.spotless") version "8.1.0"
}

group = "com.fResult"

version = "0.0.1"

description = "Practice Real-World Project for Distributed System using Kotlin and Spring Boot"

java { toolchain { languageVersion = JavaLanguageVersion.of(24) } }

repositories { mavenCentral() }

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("tools.jackson.module:jackson-module-kotlin")
  // Upgrade to Vavr 1.0.0 if available
  implementation("io.vavr:vavr:0.11.0")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
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
      """
      #!/bin/sh

      # 1. Identify staged Kotlin files
      STAGED_FILES=${'$'}(git diff --name-only --cached --diff-filter=ACMR | grep -E "\.kt${'$'}|\.kts${'$'}")

      if [ -z "${'$'}STAGED_FILES" ]; then
          exit 0
      fi

      echo "🧹 Running Spotless Apply on staged files..."

      ./gradlew spotlessApply

      RESULT=${'$'}?

      if [ ${'$'}RESULT -ne 0 ]; then
          echo "❌ Spotless check failed!"
          exit 1
      fi

      # 2. Re-stage formatted files
      echo "${'$'}STAGED_FILES" | xargs git add

      echo "✅ Code formatted successfully."
      exit 0
      """.trimIndent()

    preCommitFile.writeText(script)
    preCommitFile.setExecutable(true)
    println("Git hooks installed successfully!")
  }

  tasks.getByPath("build").dependsOn("installGitHooks")
}
