# Project Constitution - Genesys21

## Vision
Genesys21 is a high-performance, flexible e-commerce and "vitrine" builder designed to leverage the power of Kotlin Multiplatform. It aims to provide a seamless experience across Android, iOS, Web (via WasmJs), and Server-side (via Ktor), allowing small to medium businesses to manage their digital presence efficiently.

## Core Values
1. **Multiplatform Excellence**: Leverage shared code to maintain a single source of truth for business logic and data.
2. **Design Consistency**: Utilize the Genesys Design System to ensure a premium and uniform UI/UX across all targets.
3. **Quality & Stability**: Prioritize stability through automated testing and strict CI/CD pipelines.
4. **Developer Experience**: Maintain clean architecture and strict workflow rules to facilitate collaboration and maintenance.

## Technology Stack
- **Language**: Kotlin
- **Frontend Framework**: Jetpack Compose Multiplatform (Android, iOS, WasmJs)
- **Backend Framework**: Ktor (Server)
- **Database**: SQLDelight / SQLite
- **Networking**: Ktor Client
- **Serialization**: Kotlinx Serialization (Polymorphic)
- **Dependency Injection**: Koin
- **Testing**: Kotlin Test, MockK, Appium, Playwright

## Quality Standards
- **Automated Testing**: Target 80% code coverage for shared logic.
- **CI/CD Compliance**: All Pull Requests must pass full multi-target builds and tests.
- **Static Analysis**: Zero "Error" level warnings in static analysis before merging.
- **Gitflow**: Strict adherence to the Gitflow branching model. No direct commits to `main` or `develop`.

## Development Principles
1. **SOLID Architecture**: Adherence to SOLID principles and Clean Architecture (Domain, Data, Presentation layers).
2. **Test-Driven Development (TDD)**: Write tests for critical business logic and bug fixes.
3. **Surgical Edits**: Prefer small, focused code changes over large, monolithic commits.
4. **Buffer-Safe Operations**: Use IDE-safe tools for file modifications to prevent data loss.

## Constraints
- **Must Have**: Full support for WasmJs and Ktor-based backend synchronization.
- **Must Not Have**: Platform-specific logic in the `commonMain` module unless unavoidable.
- **Performance**: Mobile screens must load in under 2 seconds; Web bundle size must be optimized for fast delivery.
