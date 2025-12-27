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

I started this project as sandbox to apply concepts I have learned for since 3 years ago until now.\
Aiming to build a solid foundation for architecting real-world, scalable distributed systems in the future.

## 🗺️ Roadmap & Status

- [x] **Phase 1: The Core** (Domain Logic, Unit Tests, Validation)
- [ ] **Phase 2: The Architecture** (Hexagonal Structure, DB Schema, Outbox Pattern) - *Current Focus* 👀
- [ ] **Phase 3: Resilience** (Idempotency, Circuit Breaker, Distributed Tracing)
- [ ] **Phase 4: Reconciliation** (Background Consistency Checks)
- [ ] **Phase 5: Scaling** (Caching, Sharding strategies)

> [!note] **Future Horizons:**
> Once the Wallet core is bulletproof, I plan to introduce other domains like **Payment** (Orchestration), **Accounting** (Double-Entry Ledger), and **Notification** to simulate a full-blown microservices ecosystem

## 📚 Knowledge Lineage & Foundations

This project is not built in a vacuum. It is the culmination of intensive study and practice in **Distributed Systems** and **Modern Java/Kotlin Development**. The architecture is heavily influenced by the following resources:

### 🧠 Architectural Foundations (The "Why")

*The core philosophy of Resilience, Scalability, and Message-Driven Architecture.*

- **Mathematics for Working Programmers** (Series) by [Rawitat Pulam (Lect. Dave)](https://www.facebook.com/codeappcompany)
    - *Influence:* Served as a **Thinking Framework** for structuring complex logic\
        It shifted my paradigm from just writing imperative instructions to designing flows that are **Predictable** and **Easy to Reason About**\
        (using concepts like Equational Reasoning and State Transitions)
    - *Key Transformation:*
        - **From Accident to Intent:** Learned to eliminate ambiguity by replacing complex conditional chains with **Clear Predicates** and **State Transitions**, making the business logic explicit and readable
        - **Flow & Composition:** Adopted principles like **Low-Entropy** and **Immutability** to reduce cognitive load\
            I use concepts from **Category Theory** (like *Functors* and *Monads*) as a practical **Design Patterns** to handle side effects and data transformation cleanly (e.g., Railway Oriented Programming)
### 🛠 Implementation Mastery (The "How")
*The technical skills required to translate architecture into working code.*

- **[Reactive Spring](https://leanpub.com/reactive-spring)** by [Josh Long](https://www.linkedin.com/in/joshlong)
    - *Influence:* A valuable guide for **Testing Reactive Systems** and mastering `StepVerifier`.
    - *Re-engineering Lab:* [fResult/Learn-Spring-Webflux-3.0](https://github.com/fResult/Learn-Spring-Webflux-3.0) — *I adapted the original codebase (Java 17/Maven) to a **Bleeding Edge Stack** (Java 24, Spring Boot 3.5-4.0, Gradle Kotlin DSL)\
        The project was restructured into a **Monorepo with Composite Builds** to understand dependency management in a modern Kotlin environment*
- **[Learning Spring Boot 3.0](https://www.packtpub.com/product/learning-spring-boot-30-third-edition/9781803233307)** by [Greg L. Turnquist](https://www.linkedin.com/in/greg-l-turnquist)
  - *Influence:* Offered me a Spring Boot practical perspective on **"Convention over Configuration"** and the **Spring Application Context** 
    - *Implementation Log:* [fResult/Learning-Spring-Boot-3.0](https://github.com/fResult/Learning-Spring-Boot-3.0) — *Following the "Get Your Hands Dirty" philosophy, I manually implemented every pattern to internalize the mechanics, focusing on **Production-Grade** features like Native Images and Observability*
- **[Gout Together](https://github.com/fResult/Gout-Together) project**, learned from ([Java Backend Developer Bootcamp](https://www.youtube.com/playlist?list=PLm3A9eDaMzukMQtdDoeOR-HbFN35vieQY)), by [Thanaphoom Babparn](https://www.linkedin.com/in/thanaphoom-babparn)
    - *Influence:* An intensive series that reinforced my **Foundational Understanding** of Java and Spring Boot mechanics\
        It emphasized code discipline and helped build the essential habits needed for developing structured applications
    - *Enhanced Capstone:* [fResult/Gout-Together](https://github.com/fResult/Gout-Together) — *Beyond the curriculum, I enhanced this project by implementing **Virtual Threads** for concurrency, **Argon2** for security, and custom error handling strategies\
        It served as a proving ground for applying resilience patterns like Idempotency and Locking.*

## 🏗 Architecture Decision Records (ADR)

### Hexagonal Architecture + Domain-Driven Design (DDD)

This structure separates **Business Logic** from **Infrastructure**.\
The core domain stays pure and testable.\
External changes (e.g. DB or external API) don't affect the core domain.

### ƛ Functional Programming (FP)

The project heavily utilizes FP principles, specifically **Immutability** and **Railway Oriented Programming (ROP)** via `Either`.

#### 📖 The Visual Analogy: "Trapdoor" vs. "Railway"

To understand why we avoid Exceptions for business logic, imagine tracing a critical bug:

**1. The "Trapdoor" Nightmare (Exceptions)**

In traditional code, a function like `chargeUser()` might look successful, but deep inside, it throws an Exception.
- **The Problem:** The code execution **"teleports"** (jumps) from `chargeUser()` to a hidden `catch` block somewhere far away.
- **The Pain:** As developers, we can't *see* this jump just by reading the function signature\
    It is an **invisible trapdoor**.

**2. The "Railway" Clarity (Either)**

With `Either`, the code looks like a linear railway track:
- **The Solution:** If `chargeUser()` fails (returns `Left`), the train simply **switches tracks** to the error line.\
    It stays on the rail but bypasses the subsequent stations.
- **The Gain:** We can *see* the flow.\
    The failure path is just as explicit as the success path.\
    No teleportation, no surprises.

#### 💡 The Real-World Implementation

Instead of guessing where the code might crash, we write code that reads like a business flowchart:

#### 🧠 Personal Reflection: Why Either?

In my transition from Frontend to Backend, I found that traditional exception handling often created "Hidden Control Flows."\
The function signature fun `process(): Wallet` implies guaranteed success, but it effectively "lies" if it throws a runtime exception.

By adopting Railway-Oriented Programming, we force the function signature to tell the truth: `fun process(): Either<Failure, Wallet>`.\
This explicitly states, "I might fail, and here is exactly how," forcing the caller to handle errors as Domain Data rather than unexpected crashes.

```kotlin
// Real-world code that acts as documentation
fun processPayment(cmd: PaymentCommand): Mono<Either<Failure, PaymentReceipt>> {
  return validateRequest(cmd)      // 1. Validate
    .flatMap(::deductBalance)      // 2. Deduct Balance (Switch track if logic fails)
    .flatMap(::callBankApi)        // 3. Call Bank API (Switch track if network fails)
    .flatMap(::saveTransaction)    // 4. Save Transaction
    .flatMap(::sendEmail)          // 5. Notify User
}
```

## Prerequisites

Install these tools before you start:

- **JDK 24**: This project uses Java 24
- **Docker Engine**: **Colima** is recommended on macOS/Linux for better performance
- **Docker Compose**: Required for running the PostgreSQL database
- **Git**: Used for version control

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
