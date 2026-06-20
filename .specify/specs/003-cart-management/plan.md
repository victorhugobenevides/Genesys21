# Implementation Plan: Cart Management Consolidation

**Branch**: `003-cart-management-sync` | **Date**: 2026-06-07 | **Spec**: [.specify/specs/003-cart-management/spec.md]

## Summary
Standardize the cart lifecycle and synchronization logic across Web (WASM), Android, and iOS. Implement the merchant's ability to receive orders via WhatsApp while ensuring local persistence remains reliable.

## Technical Context

**Language/Version**: Kotlin 2.3.21

**Primary Dependencies**: Ktor 3.5.0, Koin 4.0.0, kotlinx.serialization

**Storage**: 
- **WASM**: Browser LocalStorage
- **Android**: DataStore / SharedPreferences
- **iOS/JVM**: In-Memory (initial) / Plist (future)

**Testing**: Unit tests in `:shared` for repository logic and synchronization state.

**Target Platform**: Android, iOS, Web (WASM)

## Constitution Check

1.  **Multiplatform Consistency**: PASS. Cart interface defined in `:shared`.
2.  **SDD**: PASS. Following Spec 003.
3.  **Clean Architecture**: PASS. Separating persistent logic (Data) from Cart domain.
4.  **Design System First**: PASS. Using `GenesysQuantitySelector` for updates.

## Project Structure

### Documentation
```text
.specify/specs/003-cart-management/
├── spec.md              # Feature requirements
├── plan.md              # This file
└── tasks.md             # Implementation tasks
```

### Source Code Mapping
```text
shared/src/commonMain/kotlin/.../domain/
├── model/CartItem.kt
└── repository/CartRepository.kt

shared/src/wasmJsMain/kotlin/.../data/repository/
└── LocalStorageCartRepository.kt

shared/src/androidMain/kotlin/.../data/repository/
└── AndroidCartRepository.kt

shared/src/jvmMain/kotlin/.../data/repository/
└── InMemoryCartRepository.kt
```

## Complexity Tracking
None at this stage.
