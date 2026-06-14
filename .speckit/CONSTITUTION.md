# Project Constitution: Genesys21

## 1. Vision & Purpose
Genesys21 is a premier **Kotlin Multiplatform (KMP)** vitrine builder and e-commerce engine. Our mission is to empower small and medium businesses with a high-performance digital presence that remains consistent across **Android, iOS, and Web (WasmJs)**, backed by a robust **Ktor** server infrastructure.

## 2. Core Values
*   **Multiplatform First**: All business logic, models, and data synchronization MUST reside in the `shared` module. Platform-specific code is the exception, not the rule.
*   **Design Consistency**: We strictly adhere to the **Genesys Design System**. Every component must look and feel premium, regardless of the screen size or device.
*   **Built-in Stability**: Reliability is non-negotiable. We favor compile-time safety and automated validation over manual testing.
*   **Iterative Excellence**: We embrace the **Gitflow** workflow and **Spec-Driven Development** to ensure every change is intentional and documented.

## 3. Engineering Excellence
*   **Clean Architecture**: Separation of concerns is mandatory. Use `Domain` for logic, `Data` for persistence/networking, and `Presentation` for UI.
*   **SOLID Principles**: Code must be extensible and maintainable. We prefer composition over inheritance.
*   **Test-Driven Development (TDD)**: Critical paths and bug fixes MUST be covered by regression tests.
*   **Polymorphism & Serialization**: Use strict `@SerialName` annotations for all `PageComponent` subclasses to ensure data integrity across the network.

## 4. Technology Stack
*   **Languages**: Kotlin (100%)
*   **UI Framework**: Jetpack Compose Multiplatform
*   **Backend**: Ktor Server (Netty)
*   **Persistence**: SQLDelight (Local) & Exposed (Server)
*   **Serialization**: Kotlinx Serialization (JSON)
*   **DI**: Koin
*   **Media**: Coil 3 for Multiplatform Image Loading

## 5. Quality Standards & Barriers
*   **Zero-Direct-Commit Policy**: No direct pushes to `main` or `develop`.
*   **CI/CD Hard-Gates**: Every Pull Request must pass builds for all targets and all unit tests in **CircleCI**.
*   **Static Analysis**: No "ERROR" level inspections in Android Studio. Unused imports or parameters should be removed before review.
*   **Code Coverage**: Aim for >80% coverage on the `shared` module and `ViewModel` logic.

## 6. Constraints & Guidelines
*   **Performance**: Initial screen load < 2s. Lazy loading with stable keys for all lists.
*   **Security**: Sensitive keys (Firebase, Google Services) MUST never be committed. Use CircleCI Environment Variables.
*   **Expect/Actual**: Use `expect/actual` only when a shared API (like Coil or SQLDelight) is unavailable.
