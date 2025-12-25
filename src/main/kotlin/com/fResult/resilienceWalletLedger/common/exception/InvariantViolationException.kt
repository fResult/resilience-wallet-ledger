package com.fResult.resilienceWalletLedger.common.exception

/**
 * Indicates that a core domain invariant has been breached.
 *
 * **Invariant** = A rule that must ALWAYS be true (e.g., "ID cannot be null", "Balance cannot be null").
 *
 * This exception implies a **BUG** in the code or **DATA CORRUPTION**.
 * It is NOT for user input validation errors (use [com.fResult.resilienceWalletLedger.common.error.BusinessRuleViolation] instead).
 *
 * When this exception is thrown, it means the system has reached an impossible state
 * and immediate developer intervention is required.
 */
open class InvariantViolationException(
  message: String? = "Invariant Violation",
  cause: Throwable? = null,
) : RuntimeException(message, cause)
