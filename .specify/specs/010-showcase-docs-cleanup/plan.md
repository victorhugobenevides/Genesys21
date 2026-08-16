# Implementation Plan: Showcase Reorganization & Coverage

## Phase 1: Audit & Mapping
- [x] List all components in `ui/components` and verify presence in `DesignSystemShowcaseScreen.kt`.
- [x] Add `GenesysColorField` and `GenesysDropdownField` to a new "Inputs" section.
- [x] Add `GenesysStatusPicker` to the "Action & Nav" or "Display" section.

## Phase 2: UI Reorganization (DesignSystemShowcaseScreen.kt)
- [x] Update `tabs` list to follow the new categorical structure (Foundation, Inputs, Action & Nav, Display, Feedback, Booking, Payments).
- [x] Refactor existing showcase functions into smaller, category-specific functions.
- [x] Add documentation notes (subtitles) to each section explaining when to use the component.

## Phase 3: State & Variant Expansion
- [x] Update `InputsShowcase` to show `GenesysTextField` with an error message.
- [x] Update `ActionShowcase` to show `GenesysLoadingButton` in all states (Standard, Loading, Disabled).
- [x] Ensure all `GenesysBadge` variants are displayed.

## Phase 4: Test Coverage & Verification
- [x] Update `DesignSystemSnapshotTest.kt` to iterate through all 11 showcase tabs for snapshots.
- [x] Create `MoleculesSnapshotTest.testInputs()` to cover the new color/dropdown fields.
- [x] Run `gradlew :screenshot-tests:test` to verify all snapshots.
- [x] Check for any component in `ui/components` that still doesn't have a snapshot.
