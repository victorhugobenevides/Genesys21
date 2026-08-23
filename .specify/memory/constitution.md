# Genesys21 Constitution: The Engineering Manifesto

**Version**: 2.0.0 | **Ratified**: 2026-08-22 | **Last Amended**: 2026-08-22

## Core Principles (The Tenets)

### I. Multiplatform Consistency (KMP First)
Genesys21 is a Kotlin Multiplatform (KMP) project.
- **Dogma**: 90% of business logic MUST reside in the `:shared` module.
- **Abstraction**: Platform-specific code (`expect`/`actual`) is a last resort and MUST be abstracted behind interfaces in `commonMain`.

### II. Spec-Driven Development (SDD)
Code without a spec is debt.
- **Workflow**: `Spec (What/Why)` -> `Plan (How)` -> `Tasks (Action)` -> `Verify (Quality)`.
- **Location**: All specs must be documented in `.specify/specs/`.

### III. Clean Architecture (Separation of Concerns)
The project follows a strict three-layer model:
- **Domain**: Immutable Kotlin models, repository interfaces, and pure use cases. Zero external dependencies (except `kotlinx-datetime`).
- **Data**: Implementations of repositories. Ktor for remote API, SQLDelight/Exposed for persistence.
- **Presentation**: Compose Multiplatform. ViewModels follow a unidirectional state flow (MVI-lite).

### IV. Atomic Design System (Unified Visual Language)
UI development MUST use the `com.itbenevides.genesys21.ui.components` library.
- **Hierarchy**: Atoms (indivisible) -> Molecules (functional) -> Organisms (complex) -> Templates (layout masters).
- **Encapsulation**: Third-party components (Stripe, Maps, etc.) MUST be wrapped in Genesys-branded organisms.

### V. Industrial Resilience (Platform Guarding)
Components must withstand the specific bugs of target environments.
- **WasmJS Guarding**: Inputs must handle the Samsung keyboard space-reset bug using internal state buffering.
- **Lifecycle Awareness**: ViewModels and repositories must respect KMP coroutine scopes to prevent memory leaks on Mobile/Web.

### VI. Privacy by Ephemerality (The Anonymous Tenet)
Anonymous user data is transient and fragile.
- **Storage**: Nicknames and transient addresses MUST only be stored within the context of a specific `Order` or `Appointment`.
- **Dogma**: Anonymous PII (Personally Identifiable Information) MUST NEVER be synchronized to a persistent `UserProfile` record.

### VII. Transactional Idempotency (Financial Safety)
Financial and state-changing operations must be collision-proof.
- **Idempotency Key**: Every order and payment request MUST carry a client-generated UUID.
- **Stripe Law**: No Payment Intent or Checkout Session can be created without an `Idempotency-Key` derived from the unique `orderId`.

### VIII. Unified Bridge Law (The Wasm-JS Treaty)
Wasm interoperability must be predictable.
- **Centralization**: All external JS calls MUST be defined in a single `bridge.js` file within the distribution resources.
- **Kotlin Side**: Corresponding `external` declarations must live in `.wasmJs.kt` files using `@JsFun`.

### IX. Resilient Infrastructure (Cloud Agnostic)
Deployment must be immutable and verifiable.
- **Containerization**: Everything runs in Docker. Private images are hosted in the **GitHub Container Registry (GHCR)**.
- **Diagnostics**: Pipelines must perform network checks (`nc`) and automated retries to handle intermittent cloud connectivity issues.

### X. Audit & Traceability
System state changes must be visible.
- **Auditing**: All role escalations (SuperAdmin) and sensitive deletions must be logged to the `AuditLogsTable`.
- **Logs**: Backend logs must redact sensitive PII while maintaining enough context for debugging.

## Technical Stack

- **UI Framework**: Compose Multiplatform 1.10.0+
- **Language**: Kotlin 2.3.21+ (K2 Compiler enabled)
- **Dependency Injection**: Koin 4.0.0
- **Networking**: Ktor 3.5.0 (with ContentNegotiation & Logging)
- **Database**: Exposed ORM with SQLite (using WAL mode for concurrent access)
- **CI/CD**: CircleCI with GHCR Integration and OIDC identity.

## Governance & Evolution
This document is the **Supreme Source of Truth**. Changes require:
1. A formal proposal in the `implementation_plan.artifact.md`.
2. Verification of impact across Android, iOS, Web, and Server targets.
3. Ratification by the Lead Architect (User).

---
*Genesys21: Building a resilient, multi-platform ecosystem.*
