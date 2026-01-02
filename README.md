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

## 📑 Table of Contents

- [📖 About This Project](#-about-this-project)
- [🗺️ Roadmap & Status](#-roadmap--status)
- [📚 Knowledge Lineage & Foundations](#-knowledge-lineage--foundations)
- [🏛 Key Design Concepts](#-key-design-concepts)
- [📋 Prerequisites](#-prerequisites)
- [🚀 Getting Started](#-getting-started)
- [🧪 Running Tests](#-running-tests)
- [🧹 Code Style & Hygiene](#-code-style--hygiene)

## 📖 About This Project

This is a real-world **practice project**.\
I want to master high-performance backend engineering.\
And apply the concepts I have learned.

**Background:**

Coming from a primarily **Frontend** background and recently transitioned to **Backend** development.\
I realized the need to better my understanding of distributed systems, concurrency, and resilience.\
I haven't had many opportunities to touch high-performance backend work in my previous roles.

**Goal:**

I started this project as a **deliberate practice sandbox** to rigorously apply concepts I have accumulated from 2023 until now.\
My aim is to build a solid foundation for architecting real-world, scalable distributed systems.

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

> [!quote]
>
> **Defining Performance:**\
> "When I say 'Performance', I don't just mean nanosecond latency.\
> I mean **Throughput** and **Resilience** under load.\
> A fast system that calculates money wrong is just a fast way to go bankrupt."

#### 🧘‍♂️ Core Philosophy

*The guiding principles that shape every line of code.*

- **High Reasonability & Low Entropy:** We hate accidental complexity. Code must be predictable.
- **The "War Room" Test:** During an incident, code must be readable enough that a PO/BA can point to the failure line without translation. (Code as Documentation)

#### Knowledge Lineage

*The core philosophy of Mathematical Predictability, Resilience, Scalability, and Message-Driven Architecture.*

- **[Mathematics for Working Programmers](https://www.eventpop.me/e/6425/math-for-programmers)** (Series) by [Rawitat Pulam (Lect. Dave)](https://www.facebook.com/rawitat)
    - *Influence:* Served as a **Thinking Framework** grounded in **First Principles** (Lambda Calculus vs. Turing Machine).\
        It shifted my paradigm from just writing instruction-based code to designing **Logical Structures** that are **Predictable** and **Easy to Reason About** (Equational Reasoning)
    - *Key Transformation:*
        - **From Accident to Intent:** Learned to eliminate ambiguity by replacing complex conditional chains with **Clear Predicates** and **State Transitions**, making the business logic explicit and readable
        - **Flow & Composition:** Adopted principles like **Low-Entropy** and **Immutability** to reduce cognitive load.\
            I use concepts from **Category Theory** (like *Functors* and *Monads*) as a practical **Design Patterns** to handle side effects and data transformation cleanly (e.g., Railway Oriented Programming)
- **[Lightbend Reactive Architecture](https://cognitiveclass.ai/search?q=reactive+architecture)** (Learning Paths) taught by [Wade Waldron](https://www.linkedin.com/in/wade-waldron)
    - *Influence:* Provided the foundational mental model for building **Message-Driven, Resilient, and Elastic** systems.\
        It established the core principles of the **Reactive Manifesto** and how to decouple components through asynchrony and isolation
    - *Key Paths & Concepts:*
        - **[Foundations Path](https://cognitiveclass.ai/learn/reactive-architecture-foundations):** Mastered **Domain Driven Design (DDD)** for modeling bounded contexts and the **Hexagonal Architecture** pattern, which defines the core structure of this Ledger.\
            It also emphasized **Isolation** (State, Space, Time, and Failure) to ensure system-wide resilience
        - **[Advanced Path](https://cognitiveclass.ai/learn/reactive-architecture-advanced):** Focused on **Building Scalable Systems**, specifically the trade-offs between **Consistency and Availability (CAP Theorem)** and the **Laws of Scalability** (Amdahl’s and Gunther’s Laws), which are critical for designing a high-concurrency distributed system

### 🛠 Implementation Mastery (The "How")
*The technical skills required to translate architecture into working code.*

- **[Reactive Spring](https://leanpub.com/reactive-spring)** by [Josh Long](https://www.linkedin.com/in/joshlong)
    - *Influence:* A valuable guide for **Testing Reactive Systems** and mastering `StepVerifier`
    - *Modernization Practice:* [fResult/Learn-Spring-Webflux-3.0](https://github.com/fResult/Learn-Spring-Webflux-3.0) — *I adapted the original Java 17/Maven examples into a **Bleeding Edge Stack** (Java 24, Spring Boot 3.5-4.0, Gradle Kotlin DSL).\
        Restructured as a **Monorepo with Composite Builds** to better understand modern build-tool mechanics*
- **[Learning Spring Boot 3.0](https://www.packtpub.com/product/learning-spring-boot-30-third-edition/9781803233307)** by [Greg L. Turnquist](https://www.linkedin.com/in/greg-l-turnquist)
    - *Influence:* Offered a practical perspective on **"Convention over Configuration"** and the **Spring Application Context**
    - *Implementation Log:* [fResult/Learning-Spring-Boot-3.0](https://github.com/fResult/Learning-Spring-Boot-3.0) — *Following the "Get Your Hands Dirty" philosophy, I manually implemented every pattern to internalize the framework's internal mechanics rather than relying on rote memorization*
- **[Gout Together](https://github.com/fResult/Gout-Together)** (Leaned from the[Java Backend Bootcamp [2024] YouTube Playlist](https://www.youtube.com/playlist?list=PLm3A9eDaMzukMQtdDoeOR-HbFN35vieQY)), taught by [Thanaphoom Babparn](https://www.linkedin.com/in/thanaphoom-babparn)
    - *Influence (Career):* The **Critical Milestone** that transitioned me from a learner to a capable Java Developer.\
        By rigorously reviewing concepts, I solidified the **Java & Spring Boot** expertise required to operate at a professional level
    - *Influence (Technical):* The **foundational turning point** regarding **Test Engineering**.\
        It shifted my mindset from a **"Coverage-First" lens** (satisfying CI gates) to **Behavioral Verification**.\
        This series taught me to test complex business logic effectively, ensuring tests provide **Real Value** rather than just metrics

## 🏛 Key Design Concepts

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
- **The Lie:** The signature `fun chargeUser(): Receipt` promises a Receipt, but it might blow up.
- **The Teleport:** The code execution **"teleports"** (jumps) from `chargeUser()` to a hidden `catch` block somewhere far away.
- **The Pain:** It is an **invisible trapdoor**. You can't see the error path.

**2. The "Railway" Clarity (Either)**

With `Either`, the code looks like a linear railway track:
- **The Truth:** The signature `fun chargeUser(): Either<Failure, Receipt>` explicitly states "I might fail"
- **The Switch:** If it fails (returns `Left`), the train simply **switches tracks** to the error line
- **The Flow:** It stays on the rail. No teleportation. No surprises

#### 💾 The "Commit the Failure" Strategy (Transaction Management)

A common question: *"If you catch errors as `Left`, how does `@Transactional` rollback?"*

**The Philosophy:** We distinguish between **System Errors** and **Business Results**

1. **System Failures (e.g., DB Connection Lost):**
    - These are **Exceptions**
    - They bubble up and trigger `@Transactional` **ROLLBACK**
    - The state remains consistent (nothing happened)

2. **Business Failures (e.g., Insufficient Funds):**
    - These are **Data** (`Either.Left`)
        - We **COMMIT** the transaction
        - **Why?** We must persist the "Rejection Event" (Audit Log/Outbox)
        - A bank doesn't pretend a failed transaction never happened, it records a "Declined" entry
    - **Exploration Area:** Transaction Management with `Either` & `Outbox`
        - **Rule:** For Business Errors (`Left`), NEVER use `setRollbackOnly()`. We must commit the `FailureEvent`.
        - **Rule:** For System Errors (Exceptions), allow standard Rollback.

#### 🧠 Personal Reflection: Why Either?

In my transition from Frontend to Backend, I found that traditional exception handling often created "Hidden Control Flows."\
The function signature `fun process(): Wallet` implies guaranteed success, but it effectively "lies" if it throws a runtime exception.

By adopting Railway-Oriented Programming, we force the function signature to tell the truth: `fun process(): Either<Failure, Wallet>`.\
This explicitly states, "I might fail, and here is exactly how," forcing the caller to handle errors as Domain Data rather than unexpected crashes.

#### 💡 The Real-World Implementation

Instead of guessing where the code might crash, we write code that reads like a business flowchart:

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

## 📋 Prerequisites

Install these tools before you start:

- **JDK 24**: This project uses Java 24
- **Docker Engine**: **Colima** is recommended on macOS/Linux for better performance
- **Docker Compose**: Required for running the PostgreSQL database
- **Git**: Used for version control

## 🚀 Getting Started

### 1. Setup Git Hooks

This project uses a *pre-commit hook* to enforce code style.

Run this command once to install it:

```bash
./gradlew installGitHooks
```

Run this command if you need to format code by your hand:

```bash
./gradlew spotlessApply
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
>
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
