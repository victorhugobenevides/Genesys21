# Spec: Atomic Screenshot Testing (Paparazzi + Compose Multiplatform)

## 1. Executive Summary
This specification defines the strategy for implementing **Screenshot Testing** in the Genesys21 project. To align with our **Atomic Design System**, tests will be categorized by atomic level (Atoms, Molecules, Organisms, Templates), ensuring visual consistency across all shared UI components before they reach the platform-specific targets.

## 2. Technical Stack
- **Framework**: [Paparazzi](https://github.com/cashapp/paparazzi) (for fast, JVM-only screenshot rendering without emulators).
- **Environment**: `:screenshot-tests` module (or unit tests within `:composeApp`).
- **Target**: Android JVM (as a proxy for shared Compose Multiplatform logic).

## 3. Atomic Testing Strategy

### Level 1: Atoms (Primitive Validation)
*   **Goal**: Ensure fundamental building blocks render correctly across states.
*   **Targets**: `GenesysButton`, `GenesysText`, `GenesysIconButton`, `GenesysBadge`.
*   **Variations**: Default, Focused, Disabled, Pressed, Loading.
*   **Theme Coverage**: Royal, Dark, and Custom (Primary Color override).

### Level 2: Molecules (Component Interaction)
*   **Goal**: Validate composition of multiple atoms and layout logic.
*   **Targets**: `GenesysCard`, `GenesysSearchBar`, `ProductCard`, `GenesysQuantitySelector`.
*   **Variations**: Light/Dark mode, Dynamic text lengths (overflow testing).

### Level 3: Organisms & Templates (Complex Layouts)
*   **Goal**: Verify high-level layout stability and responsiveness.
*   **Targets**: `CartContent`, `ProductDetailsContent`, `WhiteLabelContent`.
*   **Variations**: Mobile (360dp) vs Tablet/Desktop (1024dp).

### Level 4: Design System Showcase (Visual Catalog)
*   **Goal**: Provide a single "Source of Truth" page for all UI components.
*   **Targets**: `DesignSystemShowcaseScreen`.
*   **Description**: A dedicated screen within the app (accessible in debug mode) that lists every variant of every component.
*   **Snapshot Benefit**: A single snapshot of this page acts as a quick health check for the entire Design System.

## 4. Feature Requirements (FR)

### FR701: Atomic Test Suite Structure
- [ ] Create a base testing class `AtomicScreenshotTest` that provides an `AppTheme` wrapper.
- [ ] Implement `AtomsSnapshotTest.kt`, `MoleculesScreenshotTest.kt`, etc.

### FR702: Design System Showcase Screen
- [ ] Implement `DesignSystemShowcaseScreen.kt` in `:composeApp`.
- [ ] Organize components into sections: **Typography**, **Colors**, **Atoms**, **Molecules**.
- [ ] Add a visual toggle for Light/Dark mode within the showcase.

### FR703: Multilingual & Theme Matrix
- [ ] Support automated snapshot generation for multiple languages (PT, EN).
- [ ] Automated "Dark Mode vs Light Mode" comparison for every component.

### FR703: CI/CD Integration
- [ ] Integration with GitHub Actions: Fail PRs if screenshots drift more than 0.1% from master.
- [ ] Artifact storage: Upload "Diff" images for failed tests.

## 5. Success Metrics
- **Visual Regression Zero**: 0 unintended UI changes reaching production.
- **Speed**: Full design system snapshot suite runs in < 60 seconds on CI.
- **Coverage**: 100% of core Design System components (Atoms & Molecules) covered by snapshots.
