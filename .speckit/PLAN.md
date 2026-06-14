# Technical Plan: WhiteLabel Refinement & Genesys Premium Layouts (Phase 2)

## Architecture Overview
The Genesys21 architecture follows **Clean Architecture** principles, separating concerns into `domain`, `data`, and `presentation` layers within a Kotlin Multiplatform (KMP) structure. 

Phase 2 builds upon this stable foundation to deliver premium UI layouts and rich micro-interactions. The core presentation layer will utilize Compose Multiplatform's animation frameworks (`Animatable`, `animateContentSize`, `animateColorAsState`, and spring physics) to enhance the user experience. All design tokens, animations, and customModifiers will be written in `commonMain` to maintain identical multiplatform visual consistency across Android, iOS, and WasmJs.

## Components

### Component 1: Design System & Core Modifiers
**Technology**: Compose Multiplatform Animation & Canvas APIs  
**Purpose**: Exposes unified visual tokens, custom modifiers (e.g., Glassmorphic backgrounds, shimmers), and helper functions.  
**Responsibilities**:
- Provide `Modifier.glassmorphic` for frosted glass surfaces.
- Provide `shimmerBrush` for animating loading skeletons.
- Define standard spring specifications (`SpringSpec`) for layout interactions.

### Component 2: Viewer Screens Refinement
**Technology**: Jetpack Compose Multiplatform  
**Purpose**: Renders the store pages, cart, login, and product details with high fidelity.  
**Responsibilities**:
- Animating the `LoginScreen` background with shifting color gradients.
- Handling snapping scroll behavior in `HorizontalPager` and scaling carousel dots.
- Implementing item removal fade-out transitions inside `CartScreen`.

### Component 3: Editor Screens Refinement
**Technology**: Jetpack Compose Multiplatform  
**Purpose**: Manages vitrine page composition and real-time layout edits.  
**Responsibilities**:
- Visual indicator pulse animation for components under drag/reorder states.
- Displaying shimmers during image uploads or database save queries.

---

## Technology Choices

| Decision | Choice | Rationale |
|----------|--------|-----------|
| State Management | Compose StateFlow | Reactive, native to Compose, and multiplatform-ready. |
| Serialization | Polymorphic `@SerialName` | Essential for handling diverse `PageComponent` types safely over JSON. |
| Dependency Injection | Koin | Lightweight, easy to set up for KMP projects. |
| Image Loading | Coil 3 | Modern, efficient, and supports KMP/Compose Multiplatform. |
| Animation Engine | Compose Animation Spec | Native spring physics and transitions ensuring 60fps rendering across targets. |

---

## Task Breakdown

### Phase 1: Core Stability & MVP (Completed)

#### T001: Common Image Loader Expect/Actual Cleanup
- **Dependencies**: None
- **Estimated Time**: 1 hour
- **Description**: Ensure `getDiskCachePath` is correctly implemented for all targets to prevent WasmJs/iOS build failures.
- **Acceptance Criteria**:
  - [x] `ImageLoader.kt` created in `commonMain`.
  - [x] `actual` implementations verified for Android, iOS, and WasmJs.
  - [x] Build succeeds for all targets.

#### T002: Polymorphic Serialization Audit
- **Dependencies**: None
- **Estimated Time**: 2 hours
- **Description**: Verify all `PageComponent` subclasses have unique and stable `@SerialName` identifiers.
- **Acceptance Criteria**:
  - [x] `Page.kt` in `shared` module audited.
  - [x] Test case added to verify serialization/deserialization of each component type.

#### T003: WhiteLabelContent Decoupling & Optimization
- **Dependencies**: T001
- **Estimated Time**: 3 hours
- **Description**: Refactor `WhiteLabelContent` to use stable keys in `LazyColumn` and remove orphaned logic.
- **Acceptance Criteria**:
  - [x] `WhiteLabelContent.kt` extracted and compilation errors fixed.
  - [x] `GenesysLazyColumnIndexed` updated with `key` and `itemModifier` support.

#### T004: Regression Test Suite Expansion
- **Dependencies**: T002
- **Estimated Time**: 2 hours
- **Description**: Add unit tests for `WhiteLabelState` transitions and `PageViewModel` draft logic.
- **Acceptance Criteria**:
  - [x] `WhiteLabelStateTest.kt` implemented.
  - [x] `PageViewModelTest.kt` updated with draft persistence scenarios.

#### T005: Multiplatform CI/CD Verification
- **Dependencies**: T003, T004
- **Estimated Time**: 1 hour
- **Description**: Run the full CI pipeline to ensure no regressions across any platform.
- **Acceptance Criteria**:
  - [x] Android build passes.
  - [x] iOS framework links successfully.
  - [x] WasmJs browser distribution build passes.

---

### Phase 2: Design System & Core Tokens (New)

#### T006: Design System Tokens & Custom Modifiers
- **Dependencies**: None
- **Estimated Time**: 3 hours
- **Description**: Add support for glassmorphism border/shadow/frosted modifiers, shimmer brushes, and radial gradient animators.
- **Acceptance Criteria**:
  - [ ] Extension modifier `Modifier.glassmorphic()` created in `commonMain`.
  - [ ] Shimmer gradient brush helper `shimmerBrush()` implemented.
  - [ ] Radial gradient transition logic created.

---

### Phase 3: Screen Enhancements & Micro-Interactions (New)

#### T007: Premium Login Screen Redesign
- **Dependencies**: T006
- **Estimated Time**: 4 hours
- **Description**: Modify `LoginScreen.kt` to include a moving radial gradient background, a frosted glass card container, and a breathing magic icon.
- **Acceptance Criteria**:
  - [ ] Animated background shifting colors created.
  - [ ] Input fields card utilizes `Modifier.glassmorphic()`.
  - [ ] Magic login icon breathes (`scale` animated continuously).

#### T008: Responsive Product Card Scaling & Cart Feedback
- **Dependencies**: T006
- **Estimated Time**: 5 hours
- **Description**: Refactor `ProductCard` to scale up on hover/tap and display a success morph check icon when adding to cart.
- **Acceptance Criteria**:
  - [ ] Card scales to `1.03f` on tap/hover using spring animation.
  - [ ] "Add to Cart" button scales, rotates, and morphs into a green checkmark check on completion.
  - [ ] Out-of-stock badge animates slide-up.

#### T009: Animated Category Filter Chips
- **Dependencies**: T006
- **Estimated Time**: 3 hours
- **Description**: Animate color changes and layout shifts on category filters chip selection.
- **Acceptance Criteria**:
  - [ ] Chips use `animateColorAsState` for background shifts.
  - [ ] Adjacent chips slide smoothly using `Modifier.animateContentSize()`.

#### T010: Cart Screen Refactor & Stepper Animation
- **Dependencies**: T006
- **Estimated Time**: 4 hours
- **Description**: Add glassmorphic panel on Cart Screen desktop layout, animate stepper indicator dots, and implement fade-out list deletions.
- **Acceptance Criteria**:
  - [ ] Stepper dots scale up and change color dynamically on progress.
  - [ ] Summary card on desktop uses glassmorphic panels.
  - [ ] Deletions use a fade-out animation before other items reorder.

#### T011: Snappy Product Carousel & Page Indicators
- **Dependencies**: T006
- **Estimated Time**: 4 hours
- **Description**: Upgrade the product detail image carousels to use snapping transitions and size-scaling indicators.
- **Acceptance Criteria**:
  - [ ] Pager page indicators scale up active slide by `1.5x` and adjust opacities.
  - [ ] Drag-to-release triggers snappy page transitions.
  - [ ] Verification of split-pane reflow on wide screens.

#### T012: Editor Loading Skeletons & Drag Pulse Highlights
- **Dependencies**: T006
- **Estimated Time**: 5 hours
- **Description**: Implement active component highlights on drag states and skeleton shimmers in `WhiteLabelScreen`.
- **Acceptance Criteria**:
  - [ ] Shimmer loaders show during component updates or image uploads.
  - [ ] Active drag/reorder components show pulsing border highlight.

---

## Execution Timeline

**Total Phase 2 Effort**: ~28 hours

### Execution Waves:
*   **Wave 1 (Foundation)**: T006 (3h)
*   **Wave 2 (Parallel - screens)**: 
    *   T007: Login Screen (4h)
    *   T008: Product Cards (5h)
    *   T009: Category Chips (3h)
*   **Wave 3 (Parallel - checkout & editor)**:
    *   T010: Cart Refactor (4h)
    *   T011: Carousel Snapping (4h)
    *   T012: Skeletons & Editor Highlights (5h)

**Critical Path**: T006 → T008 → T010 → T012
**Parallel Execution Time**: ~12 hours (Wave 1: 3h -> Wave 2: 5h -> Wave 3: 5h)
**Time Savings**: ~16 hours (57% reduction)

---

## Risk Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Render performance lag on low-end devices | Medium | High | Optimize animations by using graphics layers (`Modifier.graphicsLayer`) to avoid frequent recompositions. |
| Platform-specific canvas crashes (WasmJs) | Low | High | Validate expect/actual paint structures on browser targets immediately during Wave 1 verification. |
