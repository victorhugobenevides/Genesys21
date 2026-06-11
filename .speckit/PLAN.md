# Implementation Plan: WhiteLabel Refinement & QA Stability

## Architecture Overview
The Genesys21 architecture follows **Clean Architecture** principles, separating concerns into `domain`, `data`, and `presentation` layers within a Kotlin Multiplatform (KMP) structure.

- **Shared Module (`:shared`)**: Contains the Domain models and the polymorphic serialization logic using `kotlinx.serialization`.
- **Compose Module (`:composeApp`)**: Handles UI across Android, iOS, and WasmJs. State is managed via `PageViewModel` and `WhiteLabelState`.
- **Backend Module (`:server`)**: Ktor-based server handling page persistence and category management.

## Technology Choices
| Decision | Choice | Rationale |
|----------|--------|-----------|
| State Management | Compose StateFlow | Reactive, native to Compose, and multiplatform-ready. |
| Serialization | Polymorphic `@SerialName` | Essential for handling diverse `PageComponent` types safely over JSON. |
| Dependency Injection | Koin | Lightweight, easy to set up for KMP projects. |
| Image Loading | Coil 3 | Modern, efficient, and supports KMP/Compose Multiplatform. |

## Task Breakdown

### Wave 1: Core Stability (Parallel)
**T001: Common Image Loader Expect/Actual Cleanup**
- **Dependencies**: None
- **Estimated Time**: 1 hour
- **Description**: Ensure `getDiskCachePath` is correctly implemented for all targets to prevent WasmJs/iOS build failures.
- **Acceptance Criteria**:
  - [x] `ImageLoader.kt` created in `commonMain`.
  - [x] `actual` implementations verified for Android, iOS, and WasmJs.
  - [x] Build succeeds for all targets.

**T002: Polymorphic Serialization Audit**
- **Dependencies**: None
- **Estimated Time**: 2 hours
- **Description**: Verify all `PageComponent` subclasses have unique and stable `@SerialName` identifiers.
- **Acceptance Criteria**:
  - [x] `Page.kt` in `shared` module audited.
  - [x] Test case added to verify serialization/deserialization of each component type.

### Wave 2: UI Refinement (Parallel)
**T003: WhiteLabelContent Decoupling & Optimization**
- **Dependencies**: T001
- **Estimated Time**: 3 hours
- **Description**: Refactor `WhiteLabelContent` to use stable keys in `LazyColumn` and remove orphaned logic.
- **Acceptance Criteria**:
  - [x] `WhiteLabelContent.kt` extracted and compilation errors fixed.
  - [x] `GenesysLazyColumnIndexed` updated with `key` and `itemModifier` support.

**T004: Regression Test Suite Expansion**
- **Dependencies**: T002
- **Estimated Time**: 2 hours
- **Description**: Add unit tests for `WhiteLabelState` transitions and `PageViewModel` draft logic.
- **Acceptance Criteria**:
  - [x] `WhiteLabelStateTest.kt` implemented.
  - [ ] `PageViewModelTest.kt` updated with draft persistence scenarios.

### Wave 3: Validation & Polish
**T005: Multiplatform CI/CD Verification**
- **Dependencies**: T003, T004
- **Estimated Time**: 1 hour
- **Description**: Run the full CI pipeline to ensure no regressions across any platform.
- **Acceptance Criteria**:
  - [ ] Android build passes.
  - [ ] iOS framework links successfully.
  - [ ] WasmJs browser distribution build passes.

## Execution Timeline

**Sequential Execution**: 9 hours

**Parallel Execution**: ~5 hours

**Time Savings**: ~4 hours (44%)

### Wave 1 (Parallel - 2h)
- T001: Image Loader Cleanup (1h)
- T002: Serialization Audit (2h)

### Wave 2 (Parallel - 3h)
- T003: UI Refactor (3h)
- T004: Testing Expansion (2h)

### Wave 3 (Sequential - 1h)
- T005: CI Verification (1h)

**Critical Path**: T002 → T003 → T005
