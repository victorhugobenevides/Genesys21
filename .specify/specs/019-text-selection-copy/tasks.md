# Tasks: Spec 019 - Text Selection & Copy

## Phase 1: Design System (Atoms)
- [ ] **T001** Update `GenesysText.kt` to support `isSelectable`.
- [ ] **T002** Implement the `SelectionContainer` wrapper logic.

## Phase 2: Design System (Molecules)
- [ ] **T003** Create `GenesysCopyableText` with a copy icon and feedback.
- [ ] **T004** Add "Copied to clipboard" localizable string to `GenesysStrings`.

## Phase 3: Screen Implementation
- [ ] **T005** Update `OrderTrackingScreen` to allow selecting the Order ID.
- [ ] **T006** Update `StoreSettingsTabUI` to make API keys and Stripe IDs copyable.
- [ ] **T007** Enable selection on `ProductDetailsScreen` descriptions.

## Phase 4: Verification
- [ ] **T008** Run Paparazzi snapshots to ensure no visual regressions in alignment.
- [ ] **T009** Verify mouse-dragging selection in WasmJS build.
