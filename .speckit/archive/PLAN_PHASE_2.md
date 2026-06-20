# Implementation Plan: Phase 2 - Premium Layout & Micro-Interactions

## Architecture Overview
Phase 2 focuses on the **Presentation Layer** refinement. We will centralize design tokens in `commonMain` to ensure cross-platform visual parity.

- **Tokens**: `ui.theme.GenesysMotion` and `ui.theme.GenesysGlass`.
- **Modifiers**: Custom KMP modifiers for `glassmorphic`, `shimmer`, and `staggeredEntry`.
- **Animations**: Heavy use of Compose `Animatable` and `Transition` APIs.

## Task Breakdown

### Wave 1: Design System Foundation (Parallel)
**T006: Core Visual Tokens & Modifiers**
- **Dependencies**: None | **Est**: 3h
- **Description**: Implement `Modifier.glassmorphic()` and `shimmerBrush()`. Centralize `SpringSpec` values.

**T013: Staggered Entry Animation Engine (NEW)**
- **Dependencies**: None | **Est**: 2h
- **Description**: Create a reusable utility for animating list items sequentially with a delay based on their index.

### Wave 2: Experience Enhancements (Parallel)
**T007: Premium Login Screen Redesign**
- **Dependencies**: T006 | **Est**: 4h
- **Description**: Shifting radial gradient background + Frosted glass login card.

**T008: Interactive Product Cards**
- **Dependencies**: T006 | **Est**: 5h
- **Description**: Hover/Tap scaling + Add-to-Cart success morphing.

**T009: Animated Category Filter Chips**
- **Dependencies**: T006 | **Est**: 3h
- **Description**: Background color morphing and smooth list reflows.

### Wave 3: Workflow & Tools (Parallel)
**T010: Cart & Stepper Refinement**
- **Dependencies**: T006 | **Est**: 4h
- **Description**: Animate progress dots and cart item removal transitions.

**T014: Dashboard-Style Floating Editor (NEW)**
- **Dependencies**: T006 | **Est**: 6h
- **Description**: Refactor WhiteLabel Editor into a floating glass panel for Desktop layouts.

**T012: Editor Loading Skeletons & Highlights**
- **Dependencies**: T006 | **Est**: 5h
- **Description**: Add pulsing handles for drag operations and skeleton shimmers for components.

---

## Execution waves

### Wave 1 (Foundation - 3h)
- T006, T013

### Wave 2 (Screens - 5h)
- T007, T008, T009

### Wave 3 (Systems - 6h)
- T010, T014, T012

**Sequential Time**: 32 hours
**Parallel Time**: ~14 hours
**Time Savings**: 56%
