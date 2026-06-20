# Tasks: Cart Management Consolidation

**Prerequisites**: `plan.md`, `spec.md`

## Phase 1: Foundational (Interface & Models)

- [x] T001 Ensure `CartItem` and `CartRepository` are aligned with the latest `Product` model
- [ ] T002 Create `BaseCartRepository` in `commonMain` to centralize flow logic and sessionId generation
- [ ] T003 Implement a multiplatform `SessionManager` or abstract the session persistence in `BaseCartRepository`

---

## Phase 2: Platform Implementations (Priority: P1)

- [ ] T003 [P] Audit and fix `LocalStorageCartRepository.kt` (WASM) to ensure immediate persistence on write
- [ ] T004 [P] Implement/Verify `AndroidCartRepository.kt` using DataStore
- [ ] T005 Implement `syncWithServer` logic to push local items to `/api/cart` using Ktor

---

## Phase 3: User Story 1 - Multiplatform Stability (Priority: P1) 🎯 MVP

**Goal**: Visitor adds items on any device and they persist across refreshes.

- [ ] T006 Add Unit Tests for `CartRepository` operations in `:shared`
- [ ] T007 [P] Implement reactive cart count in `GenesysTopAppBar` badge
- [ ] T008 [P] Integrate `GenesysQuantitySelector` in the `CartScreen`

---

## Phase 4: User Story 2 - Server Sync & Auth Merge (Priority: P2)

**Goal**: Anonymous cart items merge into user account after login.

- [ ] T009 Implement `mergeCarts` use case to handle the transition from Guest to User
- [ ] T010 Add error handling for server sync (Conflict resolution logic)

---

## Phase 5: Verification & Polish

- [ ] T011 Verify "Empty Cart" empty state visibility and behavior
- [ ] T012 Final validation of total price calculation logic (Edge case: decimal rounding)
