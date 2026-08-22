# Tasks: Spec 018 - Autocomplete Engine

## Phase 1: Shared Utilities
- [ ] **T001** Implement `FuzzySearch` utility in `shared/util`.
- [ ] **T002** Create `SuggestionProvider` interface and basic `StaticSuggestionProvider`.

## Phase 2: Design System Integration
- [ ] **T003** Create `GenesysAutocompleteField` atom in `composeApp`.
- [ ] **T004** Implement the suggestion popup with standard Genesys styling.
- [ ] **T005** Add arrow key and enter key support for selection (Desktop/Wasm).

## Phase 3: Server-Side API
- [ ] **T006** Create `/api/suggestions/products` endpoint in Ktor.
- [ ] **T007** Add caching for unique product names to minimize DB hits.
- [ ] **T008** Implement `storeId` validation to prevent data leaks.

## Phase 4: Implementation in Screens
- [ ] **T009** Update `ProductEditorScreen` categories field with Autocomplete.
- [ ] **T010** Update `PageListScreen` search bar with Autocomplete for vitrine names.
- [ ] **T011** Integrate "City/State" suggestions in `CartScreen` address form.

## Phase 5: Verification
- [ ] **T012** Run performance profiling in WasmJS while typing rapidly.
- [ ] **T013** Unit test the `FuzzySearch` with complex UTF-8 characters (Accents).
