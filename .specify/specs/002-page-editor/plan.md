# Implementation Plan: Page Editor Refactoring & Validation

**Branch**: `002-page-editor-sync` | **Date**: 2026-06-07 | **Spec**: [.specify/specs/002-page-editor/spec.md]

## Summary
Consolidate the "White Label" Page Editor by aligning the recently restored `PageComponent` models with the `PageComponentRenderer` and implementing a robust validation layer for reordering and draft management.

## Technical Context

**Language/Version**: Kotlin 2.3.21

**Primary Dependencies**: Compose Multiplatform 1.10.0, Koin 4.0.0, Ktor 3.5.0

**Storage**: `PageDraftRepository` (Local Persistence) and `PageRepository` (Remote Persistence)

**Testing**: Unit tests in `:shared` (Models/UseCases) and Screenshot tests in `:screenshot-tests` (UI consistency)

**Target Platform**: Android, iOS, Web (WASM)

**Project Type**: Multiplatform Storefront Builder

**Performance Goals**: Live preview updates < 100ms, Publish sync < 2s

## Constitution Check

1.  **Multiplatform Consistency**: PASS. Models are in `:shared` and UI logic in `commonMain`.
2.  **SDD**: PASS. Started with `002-page-editor/spec.md`.
3.  **Clean Architecture**: PASS. Domain models strictly separated from rendering logic.
4.  **Design System First**: PASS. Using `Genesys*` component library.
5.  **Industrial Resilience**: PASS. Text inputs use integrity buffers for WASM stability.

## Project Structure

### Documentation (this feature)
```text
.specify/specs/002-page-editor/
├── spec.md              # Feature requirements
├── plan.md              # This file
└── tasks.md             # To be generated
```

### Source Code Mapping
```text
shared/src/commonMain/kotlin/.../domain/model/
├── Page.kt              # Component models (Restored)
├── PageThemeConfig.kt   # Theme definitions

composeApp/src/commonMain/kotlin/.../presentation/screens/viewer/
├── PageComponentRenderer.kt # The "Live Preview" engine

composeApp/src/commonMain/kotlin/.../presentation/screens/editor/
├── WhiteLabelScreen.kt  # Main Editor UI
├── *ComponentEditor.kt  # Individual component editors
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Duplicate fields in subclasses | Required for `json.encodeToString` polymorphic serialization while keeping a shared base interface. | Interface-only properties are ignored by `kotlinx-serialization` during direct encoding. |
