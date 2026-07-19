# Genesys Design System Specification

## Overview
The Genesys Design System is a set of standardized UI components built with Compose Multiplatform. It ensures visual consistency and high resilience across Android, iOS, and Web.

## Core Values
- **Semantic Components**: Components are named by their function (`GenesysPage`, `GenesysLoadingButton`) rather than their implementation.
- **Platform Resilience**: Native components are wrapped with guards to handle platform bugs (e.g., the WasmJs spacebar bug in TextFields).
- **Theme-Aware**: All components consume the `MaterialTheme` color scheme and typography.

## Standardized Tokens
- **GenesysDimens**: Standardized spacing (`Small`, `Medium`, `Large`, `ExtraLarge`), icon sizes, and corner radii.
- **GenesysTextStyle**: Semantic typography levels (`Display`, `Headline`, `Title`, `Body`, `Label`).
- **GenesysIcons**: A unified set of icons mapped to material or custom assets.

## Primary Components

### 1. Layout & Aesthetics
- `GenesysPage`: Top-level scaffold with unified top bars and snackbar support.
- `GenesysColumn` / `GenesysRow`: Standard layout containers with consistent arrangement and padding defaults.
- **Glassmorphism**: Use translucent surfaces (`copy(alpha = 0.7f)`) with backdrop blur approximations and thin borders (`0.5.dp`) for premium cards and overlays.
- **Micro-Animations**: Spring-based physics (`Spring.DampingRatioMediumBouncy`) for scaling effects on interactive cards and buttons.

### 2. Inputs
- `GenesysTextField`: Resilient text input with internal state buffering for stability.
- `GenesysSearchBar`: Specialized input for filtering lists.
- `GenesysQuantitySelector`: Horizontal stepper for adjusting item counts.

### 3. Feedback & Data
- `GenesysLoadingButton`: Button with integrated loading state and icon support.
- `GenesysEmptyState`: Standard UI for empty results or search failures.
- `GenesysCard`: Container for grouping information with standard elevation and padding.

## Acceptance Criteria
- Components must support dark and light modes correctly.
- Interactive elements must provide appropriate visual feedback (ripples, scales).
- Components must be documented with `@Preview` annotations in `:composeApp`.
