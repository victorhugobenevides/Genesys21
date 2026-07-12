# Implementation Plan: Atomic Screenshot Testing

## 1. Architecture
We will utilize **Paparazzi** within the `screenshot-tests` module to render Compose components on the JVM. This avoids the overhead of Android Emulators while maintaining high-fidelity rendering for our shared Multiplatform UI.

## 2. Task Breakdown

### Wave 1: Foundation (Tokens & Setup)
**T032: Paparazzi Integration & Config**
- **Description**: Configure `screenshot-tests/build.gradle.kts` with the Paparazzi plugin.
- **Goal**: Successfully run `./gradlew :screenshot-tests:recordPaparazziDebug` for a "Hello World" component.

**T033: Theme Wrapper Helper**
- **Description**: Create a test utility `GenesysPaparazzi` that automatically wraps components in `AppTheme` and provides a standard device configuration.

### Wave 2: Atomic Suite
**T034: Atoms Snapshot Suite**
- **Description**: Implement snapshot tests for all components in `ui.components.atoms`.
- **States**: Default, Primary, Small/Large, Disabled.

**T035: Molecules Snapshot Suite**
- **Description**: Implement snapshot tests for all components in `ui.components.molecules`.
- **Focus**: `ProductCard` with price/stock variations, `CartItemRow`.

**T038: Design System Showcase Implementation**
- **Description**: Create the `DesignSystemShowcaseScreen` with organized sections for all components.
- **Integration**: Add a navigation entry to access the Showcase from the Dashboard or Splash (Debug only).

### Wave 3: Responsiveness & Regression
**T036: Multi-Device Validation**
- **Description**: Create specific tests for `Organisms` using multiple Paparazzi configurations (Pixel 5, Nexus 10, Desktop).

**T037: CI Pipeline Enforcement**
- **Description**: Update `.github/workflows/ci.yml` to run `verifyPaparazziDebug` on all Pull Requests.

---

## Execution waves

### Wave 1 (Setup - 4h)
- T032, T033

### Wave 2 (Atomic - 8h)
- T034, T035

### Wave 3 (CI/CD - 4h)
- T036, T037
