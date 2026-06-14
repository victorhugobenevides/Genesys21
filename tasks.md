# Implementation Tasks: Premium Layout & Micro-Interactions (Phase 2)

Este arquivo contém a lista ordenada de tarefas para a implementação da Fase 2. Cada tarefa deve ser executada em sua própria branch seguindo o fluxo de Gitflow.

## Wave 1: Foundation (Design System Tokens) - COMPLETED

- [x] **T006: Design System Tokens & Custom Modifiers**
    - **GitHub Issue**: #82
    - **Description**: Implement `Modifier.glassmorphic()` and `shimmerBrush()`. Centralize `SpringSpec` values.
    - **Acceptance Criteria**:
        - [x] Glassmorphic modifier available in `ui.util`.
        - [x] Shimmer brush helper available for skeleton loaders.

- [x] **T013: Staggered Entry Animation Engine**
    - **GitHub Issue**: #89
    - Description: Create a reusable utility for animating list items sequentially.
    - **Acceptance Criteria**:
        - [x] `Modifier.staggeredEntry()` implemented in `ui.util`.

---

## Wave 2: Screen Enhancements (Visual Identity) - COMPLETED

- [x] **T007: Premium Login Screen Redesign**
    - **GitHub Issue**: #83
    - **Dependencies**: T006
    - **Description**: Shifting radial gradient background + Frosted glass login card.
    - **Acceptance Criteria**:
        - [x] Shifting background gradient active on Login.
        - [x] Frosted input container implemented.

- [x] **T008: Interactive Product Cards**
    - **GitHub Issue**: #84
    - **Dependencies**: T006
    - **Description**: Hover/Tap scaling + Add-to-Cart success morphing.
    - **Acceptance Criteria**:
        - [x] Spring-based scaling on interaction.
        - [x] Success icon morphing on add to cart.

- [x] **T009: Animated Category Filter Chips**
    - **GitHub Issue**: #85
    - **Dependencies**: T006
    - **Description**: Background color morphing and smooth list reflows.
    - **Acceptance Criteria**:
        - [x] Smooth color transitions on selection.
        - [x] Animated layout shifts using `animateContentSize`.

---

## Wave 3: Systems & Advanced Interactions - COMPLETED

- [x] **T010: Cart & Stepper Refinement**
    - **GitHub Issue**: #86
    - **Dependencies**: T006
    - **Description**: Animate progress dots and cart item removal transitions.
    - **Acceptance Criteria**:
        - [x] Stepper dots scale dynamically.
        - [x] Cart item removal uses fade + slide animation.

- [x] **T011: Snappy Product Carousel & Page Indicators**
    - **GitHub Issue**: #87
    - **Dependencies**: T006
    - **Description**: Upgrade carousels with page-snapping and scaling dots.
    - **Acceptance Criteria**:
        - [x] Snappy transitions in `HorizontalPager`.
        - [x] Active dot scaling to 1.5x radius.

- [x] **T014: Dashboard-Style Floating Editor (Desktop)**
    - **GitHub Issue**: #90
    - **Dependencies**: T006
    - **Description**: Refactor WhiteLabel Editor into a floating glass panel.
    - **Acceptance Criteria**:
        - [x] Desktop layout (>1000dp) uses floating card.
        - [x] Seamless transition from Mobile UI.

- [x] **T012: Editor Loading Skeletons & Highlights**
    - **GitHub Issue**: #88
    - **Dependencies**: T006
    - **Description**: Add pulsing handles for drag operations and skeleton shimmers.
    - **Acceptance Criteria**:
        - [x] Shimmers active during component loading.
        - [x] Pulsing highlight on active reorder handle.

## Wave 4: DevOps & Quality Hard-Gates (NEW)

- [x] **T015: CircleCI Artifacts & Coverage Reporting**
    - **GitHub Issue**: #91
    - **Description**: Configure CircleCI to store test results, linting reports, and code coverage as build artifacts.
    - **Acceptance Criteria**:
        - [ ] JUnit XML results visible in CircleCI Tests tab.
        - [ ] Coverage HTML reports accessible as artifacts.
        - [ ] Linting (Ktlint/Detekt) reports preserved.
