# Resilience Wallet Ledger

This is a Fintech Ledger system.\
It focuses on **Resilience**, **Consistency**, and **High Concurrency**.\
It is built with Kotlin and Spring Boot WebFlux.\
It follows DDD and Hexagonal Architecture principles.

## 📖 About This Project

This is a **practice project**.\
I want to master high-performance backend engineering.

**Background:**

Coming from a primarily **Frontend** background and recently transitioned to **Backend** development.\
I realized the need to better my understanding of distributed systems, concurrency, and resilience.\
I haven't had many opportunities to touch high-performance backend work in my previous roles.

**Goal:**

I started this project as sandbox to apply concepts I have learned for since 2 years until now.\
Aiming to build a solid foundation for architecting real-world, scalable distributed systems in the future.

## 🏗 Architecture Decision Records (ADR)

### Hexagonal Architecture + Domain-Driven Design (DDD)

This structure separates **Business Logic** from **Infrastructure**.\
The core domain stays pure and testable.\
External changes (e.g. DB or external API) don't affect the core domain.

### ƛ Functional Programming (FP)

The project uses FP principles like Immutability and `Either`.\
This enables our code predictable, composable, and reduces side effects.

**Why this combination?**

The aim is to connect code and business.\
Code is written to look like business rules.

**Business people should be able to read the flow**.\
They can help engineer to fix bugs.\
This ensures the software does what the business needs.

The ultimate goal is to bridge the gap between technical implementation and business understanding.

## Prerequisites

Install these tools before you start:

- **JDK 24**: This project uses Java 24.
- **Docker Engine**: **Colima** is recommended on macOS/Linux for better performance.
- **Docker Compose**: Required for running the PostgreSQL database.
- **Git**: Used for version control.

## 🚀 Getting Started

### 1. Setup Git Hooks

This project uses a *pre-commit hook* to enforce code style.\

Run this command once to install it:

```bash
./gradlew installGitHooks
```

Run this command if you need to format code by your hand:

```bash
./gradlew spotlessApply
```

```bash
./gradlew 
```

### 2. Setup Docker Environment (Colima)

If you use macOS with Colima, start it first.\
Make sure you give it enough CPU and RAM.

```bash
# Start colima with 4 CPUs and 8GB RAM (Adjust as needed)
colima start --cpu 4 --memory 8
```

### 3. Start Infrastructure

Use Docker Compose to start the database:

```bash
# Start PostgreSQL database
docker compose up -d
```
 
### 4. Run the Application
 
Run the Spring Boot application via Gradle wrapper:

```bash
./gradlew bootRun
```

The server will start on port `8080` (Default).

## 🧪 Running Tests

This project is tested heavily.\
It uses Unit and Integration Tests.

> [!note]
> Integration Tests utilize **Testcontainers**, which requires a running Docker environment.

```bash
# Run all tests
./gradlew test
```

## 🧹 Code Style & Hygiene

Code style is enforced to ensure consistency.\
The project uses **Spotless**, **Ktlint**, and **EditorConfig**.\
A Pre-commit Hook checks files before committing.

To manually format the code:

```bash
./gradlew spotlessApply
```
