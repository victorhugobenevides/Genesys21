# Spec 010: Design System Showcase Reorganization & Documentation

## 1. Overview
The **Genesys21 Design System Showcase** (located at `/about`) is the central hub for developers and merchants to explore available UI components. As the project evolved with the Booking System and Auth features, several new components were added but not properly mapped or documented in the showcase. This spec aims to reorganize the showcase, map missing components, and ensure high test coverage.

## 2. Core Objectives
- **Mapping**: Include all missing Atoms, Molecules, and Organisms in the Showcase.
- **Reorganization**: Group components logically (e.g., Input, Navigation, Feedback, Booking).
- **Documentation**: Add brief descriptions, usage guidelines, and code snippets (if applicable) for each component category.
- **Test Coverage**: Ensure all showcase tabs and individual critical components have screenshot tests (Paparazzi).
- **State Variations**: Show components in different states (Loading, Disabled, Error, Selected).

## 3. Components to Map
### 3.1. Missing Molecules
- `GenesysColorField`: Component for color selection with presets.
- `GenesysDropdownField`: Optimized dropdown for WasmJs.
- `GenesysStatusPicker`: Order status selector.

### 3.2. Unmapped States
- `GenesysLoadingButton`: Ensure "Loading" and "Disabled" states are visible side-by-side.
- `GenesysTextField`: Show "Error" state.
- `GenesysTabRow`: Show variations with different badge counts.

### 3.3. Booking System
- Ensure `ServiceCard` is explicitly shown outside of the renderer.
- Add `AppointmentNote` component if it becomes a standalone atom/molecule.

## 4. Architecture & Technology Stack

### 4.1. Core Technologies
- **UI Framework**: Compose Multiplatform (KMP) for Android, iOS, and WasmJS.
- **Dependency Injection**: Koin for Multiplatform.
- **Networking**: Ktor Client for type-safe API communication.
- **Concurrency**: Kotlin Coroutines & Flow for reactive state management.
- **Time Management**: `kotlinx-datetime` for booking logic.
- **Authentication**: Firebase Auth with platform-specific bridges (including JS/Wasm).
- **Screenshot Testing**: Paparazzi (Android-based snapshotting).

### 4.2. Clean Architecture (KMP Approach)
The project is divided into three main layers within the `shared` and `composeApp` modules:
1. **Presentation (UI)**: Jetpack Compose components following Atomic Design. ViewModels manage UI state using `StateFlow`.
2. **Domain**: Pure Kotlin logic. Contains `UseCases`, `Models` (Entities), and `Repository` interfaces. This is the "brain" of the app, independent of platforms.
3. **Data**: Implementation of repository interfaces. Uses Ktor for API calls and platform-specific drivers for persistence.

## 5. Genesys Design System (Atomic Design)
We follow the **Atomic Design** methodology to ensure consistency, reuse, and scalability:

- **Atoms**: The smallest building blocks (Typography, Icons, Spacers, Badges). They cannot be broken down further without losing their purpose.
- **Molecules**: Groups of atoms bonded together (e.g., a `GenesysTextField` which combines a label atom, an icon atom, and an input primitive).
- **Organisms**: Complex UI components composed of groups of molecules and/or atoms (e.g., `GenesysBookingEngine` or `GenesysProductList`).
- **Templates**: Page-level structures that define the layout (e.g., `GenesysPage`).

## 6. Reorganization Plan
### 4.1. New Tab Structure
1. **Foundation**: Typography, Colors, Tokens, Icons.
2. **Inputs**: TextFields, Sliders, Chips, Color/Dropdown Fields.
3. **Action & Nav**: Buttons, Tabs, Pagers, Fabs.
4. **Display**: Cards, Lists, Images, Badges.
5. **Feedback**: Empty States, Loading, Dialogs, Timelines.
6. **Booking**: Calendar, TimePicker, BookingEngine.

## 5. Technical Requirements
- **Screenshot Tests**: Update `DesignSystemSnapshotTest.kt` to cover all new tabs.
- **Theme Support**: Ensure the showcase works correctly across all `PageThemeConfig` variants.
- **Responsiveness**: Verify the showcase layout on Mobile vs Desktop.

## 6. Success Criteria
- [ ] All `.kt` files in `ui/components` are represented in the Showcase.
- [ ] `DesignSystemShowcaseScreen` is reorganized into the new tab structure.
- [ ] 100% of Showcase tabs have a corresponding Paparazzi snapshot.
- [ ] `GenesysColorField` and `GenesysDropdownField` are visible and interactive in the showcase.
