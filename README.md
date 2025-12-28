# Resilience Wallet Ledger

This is a Fintech Ledger system.\
It focuses on **Resilience**, **Consistency**, and **High Concurrency**.\
It is built with Kotlin and Spring Boot WebFlux.\
It follows DDD and Hexagonal Architecture principles.

> [!warning]
> 
> **⚠️ Educational Purpose Only**\
> This repository is a Personal Sandbox & Reference Implementation.\
> While the code is open for study, I do not accept Pull Requests or provide support.\
> It serves as a proof-of-concept for modern resilient architecture patterns.

## 📖 About This Project

This is a real-world **practice project**.\
I want to master high-performance backend engineering.\
And apply the concepts I have learned.

**Background:**

Coming from a primarily **Frontend** background and recently transitioned to **Backend** development.\
I realized the need to better my understanding of distributed systems, concurrency, and resilience.\
I haven't had many opportunities to touch high-performance backend work in my previous roles.

**Goal:**

I started this project as a sandbox to apply concepts I have learned from 2023 until now.\
Aiming to build a solid foundation for architecting real-world, scalable distributed systems in the future.

## 🗺️ Roadmap & Status

- [x] **Phase 1: The Core** (Domain Logic, Unit Tests, Validation)
- [ ] **Phase 2: The Architecture** (Hexagonal Structure, DB Schema, Outbox Pattern) - 👀 ***Current Focus***
- [ ] **Phase 3: Resilience** (Idempotency, Circuit Breaker, Distributed Tracing)
- [ ] **Phase 4: Reconciliation** (Background Consistency Checks)
- [ ] **Phase 5: Scaling** (Caching, Sharding strategies)

> [!note]
>
> **Future Horizons:**
> Once the Wallet core is bulletproof, I plan to introduce other domains like **Payment** (Orchestration), **Accounting** (Double-Entry Ledger), and **Notification** to simulate a full-blown microservices ecosystem

## 📚 Knowledge Lineage & Foundations

This project is not built in a vacuum. It is the culmination of intensive study and practice in **Distributed Systems** and **Modern Java/Kotlin Development**. The architecture is heavily influenced by the following resources:

### 🧠 Architectural Foundations (The "Why")

*The core philosophy of Mathematical Predictability, Resilience, Scalability, and Message-Driven Architecture.*

- **[Mathematics for Working Programmers](https://www.eventpop.me/e/6425/math-for-programmers)** (Series) by [Rawitat Pulam (Lect. Dave)](https://www.facebook.com/rawitat)
    - *Influence:* Served as a **Thinking Framework** for structuring complex logic.\
        It shifted my paradigm from just writing imperative instructions to designing flows that are **Predictable** and **Easy to Reason About**.\
        (using concepts like Equational Reasoning and State Transitions)
    - *Key Transformation:*
        - **From Accident to Intent:** Learned to eliminate ambiguity by replacing complex conditional chains with **Clear Predicates** and **State Transitions**, making the business logic explicit and readable
        - **Flow & Composition:** Adopted principles like **Low-Entropy** and **Immutability** to reduce cognitive load.\
            I use concepts from **Category Theory** (like *Functors* and *Monads*) as a practical **Design Patterns** to handle side effects and data transformation cleanly (e.g., Railway Oriented Programming)
- **[Lightbend Reactive Architecture](https://cognitiveclass.ai/learn/reactive-architecture-advanced)** taught by [Wade Waldron](https://www.linkedin.com/in/wade-waldron)
    - *Influence:* Provided the foundational mental model for building **Message-Driven, Resilient, and Elastic** systems.\
        It established the core principles of the **Reactive Manifesto** and how to decouple components through asynchrony and isolation
    - *Key Paths & Concepts:*
        - **Foundations Path:** Mastered **Domain Driven Design (DDD)** for modeling bounded contexts and the **Hexagonal Architecture** pattern, which defines the core structure of this Ledger.\
            It also emphasized **Isolation** (State, Space, Time, and Failure) to ensure system-wide resilience
        - **Advanced Path:** Focused on **Building Scalable Systems**, specifically the trade-offs between **Consistency and Availability (CAP Theorem)** and the **Laws of Scalability** (Amdahl’s and Gunther’s Laws), which are critical for designing a high-concurrency distributed system

### 🛠 Implementation Mastery (The "How")
*The technical skills required to translate architecture into working code.*

- **[Reactive Spring](https://leanpub.com/reactive-spring)** by [Josh Long](https://www.linkedin.com/in/joshlong)
    - *Influence:* A valuable guide for **Testing Reactive Systems** and mastering `StepVerifier`
    - *Modernization Practice:* [fResult/Learn-Spring-Webflux-3.0](https://github.com/fResult/Learn-Spring-Webflux-3.0) — *I adapted the original Java 17/Maven examples into a **Bleeding Edge Stack** (Java 24, Spring Boot 3.5-4.0, Gradle Kotlin DSL).\
        Restructured as a **Monorepo with Composite Builds** to better understand modern build-tool mechanics*
- **[Learning Spring Boot 3.0](https://www.packtpub.com/product/learning-spring-boot-30-third-edition/9781803233307)** by [Greg L. Turnquist](https://www.linkedin.com/in/greg-l-turnquist)
    - *Influence:* Offered me a Spring Boot practical perspective on **"Convention over Configuration"** and the **Spring Application Context**
    - *Implementation Log:* [fResult/Learning-Spring-Boot-3.0](https://github.com/fResult/Learning-Spring-Boot-3.0) — *Following the "Get Your Hands Dirty" philosophy, I manually implemented every pattern to internalize the framework's internal mechanics rather than relying on rote memorization*
- **[Gout Together](https://github.com/fResult/Gout-Together)** ([Java Backend Bootcamp [2024] YouTube Playlist](https://www.youtube.com/playlist?list=PLm3A9eDaMzukMQtdDoeOR-HbFN35vieQY)) by [Thanaphoom Babparn](https://www.linkedin.com/in/thanaphoom-babparn)
    - *Influence (Career):* An intensive series that built my **Confidence** as a Java Developer.\
        By coding along and heavily reviewing concepts, I gained the solid **Java & Spring Boot** understanding that allowed me to **ace technical interviews and land my Java Developer (Backend) role**.
    - *Influence (Technical):* The **foundational turning point** for my understanding of **Real-World Unit Testing**.\
        While I had encountered testing before, I often viewed it through a **"Coverage-First" lens** due to my own knowledge gaps—prioritizing metrics over behavioral verification.\
        This series bridged that gap, teaching me to move beyond just "satisfying CI gates" to testing complex business logic effectively.\
        It ensures my tests provide **Real Value** and long-term maintainability
    - *Enhanced Practice:* [fResult/Gout-Together](https://github.com/fResult/Gout-Together?tab=readme-ov-file#my-summary) — *The codebase from my intensive bootcamp, which I further enhanced by exploring **Virtual Threads** for concurrency, **Argon2** for security, custom error handling strategies, and more beyond the standard curriculum*

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
