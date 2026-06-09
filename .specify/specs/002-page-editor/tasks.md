# Tasks: Page Editor Refactoring & Validation

**Prerequisites**: `plan.md`, `spec.md`

## Phase 1: Foundational (Model & Registry Alignment)

- [x] T001 Restore `PageComponent` subclasses in `shared/.../domain/model/Page.kt`
- [x] T002 Update `PageThemeConfig` enum with all 21 color schemes
- [x] T003 Ensure `PageComponentRenderer` covers all restored `PageComponent` types (`ProfileHeader`, `SocialLinks`, etc.)
- [x] T004 Verify polymorphic serialization of `PageComponent` via `json.encodeToString`

---

## Phase 2: User Story 1 - Component Management (Priority: P1) 🎯 MVP

**Goal**: Merchant can reorder and delete components without losing state.

### Implementation for User Story 1

- [x] T005 [P] [US1] Implement Move Up/Down logic in `WhiteLabelScreen.kt`
- [x] T006 [P] [US1] Implement Delete logic in `WhiteLabelScreen.kt`
- [x] T007 [US1] Add reordering animation to `GenesysLazyColumnIndexed` in `WhiteLabelScreen.kt`
- [x] T008 [US1] Unit test reordering logic in `shared/.../domain/usecase/UseCaseTests.kt`

---

## Phase 3: User Story 2 - Real-time Preview (Priority: P2)

**Goal**: Merchant sees an identical preview of the published page.

### Implementation for User Story 2

- [x] T009 [US2] Align `ComponentWrapperUI` padding/margins with `PageComponentRenderer`
- [x] T010 [US2] Implement "Edit Mode" overlays in `PageComponentRenderer` to avoid code duplication between Viewer and Editor
- [x] T011 [US2] Verify theme switching updates `MaterialTheme` colors instantly in the preview

---

## Phase 4: User Story 3 - Persistence & Publish (Priority: P3)

**Goal**: Local drafts are preserved and can be pushed to the server.

### Implementation for User Story 3

- [x] T012 [US3] Implement `PageDraftRepository` logic to handle auto-save on every change
- [x] T013 [US3] Connect "Publish" button to `SavePageUseCase` with `isEditing = true`
- [x] T014 [US3] Add "Discard Draft" confirmation dialog

---

## Phase 5: Verification & Polish

- [ ] T015 [P] Run screenshot tests for all 21 themes in `:screenshot-tests`
- [ ] T016 Verify WasmJs stability for `ProfileHeaderComponentEditor` text fields
- [ ] T017 Final manual E2E test: Create -> Edit -> Move -> Publish -> View Public URL
