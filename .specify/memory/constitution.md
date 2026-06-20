# Genesys21 Constitution

## Core Principles

### I. Multiplatform Consistency
Genesys21 is a Kotlin Multiplatform (KMP) project. Code should be shared as much as possible in the `:shared` module. Platform-specific implementations (`expect`/`actual`) should be minimized and abstracted behind interfaces.

### II. Spec-Driven Development (SDD)
Every new feature or major refactor must begin with a specification in the `.specify/specs/` directory. Implementation follows a Plan -> Tasks -> Execute cycle.

### III. Clean Architecture
The project follows Clean Architecture principles:
- **Domain**: Pure Kotlin models, repository interfaces, and use cases.
- **Data**: Repository implementations (Ktor for remote, SQLDelight/InMemory for local).
- **Presentation**: Compose Multiplatform UI components and ViewModels (MVI-lite pattern).

### IV. Design System First
UI development must prioritize the use of the `com.itbenevides.genesys21.ui.components` library. New components must be generalized and added to the design system before being used in specific screens.

### V. Industrial Resilience
Components (especially inputs like `GenesysTextField`) must be built to handle platform-specific bugs (e.g., Samsung keyboard buffer issues in WasmJS) using internal state guarding and integrity buffers.

## Technical Stack

- **UI**: Compose Multiplatform 1.10.0
- **Language**: Kotlin 2.3.21
- **Dependency Injection**: Koin 4.0.0
- **Networking**: Ktor 3.5.0
- **Serialization**: kotlinx.serialization
- **Image Loading**: Coil 3.1.0
- **Backend**: Ktor Server with Netty & Exposed (SQLite)

## Development Workflow

### 1. Specification
Define the "What" and "Why" in `.specify/specs/`. Use `/speckit.specify`.

### 2. Planning
Detail the "How" in a `.plan.md` file. Use `/speckit.plan`.

### 3. Task Execution
Break down the plan into actionable tasks. Use `/speckit.tasks`.

### 4. Verification
All features must be verified via automated tests (Unit tests in `:shared` and `:composeApp`, Screenshot tests in `:screenshot-tests`) before deployment.

## Governance
This constitution is the source of truth for engineering practices in Genesys21. Changes require a proposal and verification of impact across all supported platforms (Android, iOS, Web, Server).

**Version**: 1.0.0 | **Ratified**: 2026-06-07 | **Last Amended**: 2026-06-07
