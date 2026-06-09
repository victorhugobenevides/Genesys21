<!-- SPECKIT START -->
# Genesys21 Agent Context

## Project Summary
Genesys21 is a high-resilience Kotlin Multiplatform (KMP) storefront platform. It enables merchants to build and publish custom storefronts using a component-based "White Label" editor.

## Architecture
- **:shared**: Common module containing Domain models, Repository interfaces, and Use Cases.
- **:composeApp**: Unified UI module targeting Android, iOS, and Web (WasmJS).
- **:server**: Ktor-based backend providing REST APIs for pages, products, orders, and authentication.

## Core Features
1. **White Label Editor**: Live WYSIWYG editor with reorderable components.
2. **Multi-Target Viewer**: Shared rendering logic for storefronts across Web and Mobile.
3. **Cart & Order System**: Cross-platform cart management with server-side persistence.
4. **Resilient Design System**: UI components with built-in guards for platform-specific edge cases.

## Key Principles
- **Clean Architecture**: Strictly separate Domain, Data, and Presentation layers.
- **SDD (Spec-Driven Development)**: All work starts in `.specify/specs/`.
- **UI Consistency**: Use `Genesys*` components for all layouts and inputs.
- **Wasm Stability**: Maintain internal state buffers in text fields to prevent cursor/value loss.

## Project Structure
- `.specify/specs/`: Current feature specifications.
- `.specify/memory/constitution.md`: Core principles and governance.
- `composeApp/src/commonMain/kotlin/.../ui/components/`: Design system source.
- `shared/src/commonMain/kotlin/.../domain/model/`: Shared data structures.

<!-- SPECKIT END -->
