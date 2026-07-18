# Implementation Plan: Showcase Reorganization & Coverage

## Phase 1: Audit & Mapping
- [ ] List all components in `ui/components` and verify presence in `DesignSystemShowcaseScreen.kt`.
- [ ] Add `GenesysColorField` and `GenesysDropdownField` to a new "Inputs" section.
- [ ] Add `GenesysStatusPicker` to the "Action & Nav" or "Display" section.

## Phase 2: UI Reorganization (DesignSystemShowcaseScreen.kt)
- [ ] Update `tabs` list to follow the new categorical structure (Foundation, Inputs, Action & Nav, Display, Feedback, Booking).
- [ ] Refactor existing showcase functions (`AtomsShowcase`, `MoleculesShowcase`, etc.) into smaller, category-specific functions (e.g., `InputsShowcase`, `ActionShowcase`).
- [ ] Add documentation notes (subtitles) to each section explaining when to use the component.

## Phase 3: State & Variant Expansion
- [ ] Update `InputsShowcase` to show `GenesysTextField` with an error message.
- [ ] Update `ActionShowcase` to show `GenesysLoadingButton` in all states (Standard, Loading, Disabled).
- [ ] Ensure all `GenesysBadge` variants are displayed.

## Phase 4: Test Coverage & Verification
- [ ] Update `DesignSystemSnapshotTest.kt` to iterate through all showcase tabs for snapshots.
- [ ] Create `MoleculesSnapshotTest.testInputs()` to cover the new color/dropdown fields.
- [ ] Run `gradlew :screenshot-tests:test` to verify all snapshots.
- [ ] Check for any component in `ui/components` that still doesn't have a snapshot.
