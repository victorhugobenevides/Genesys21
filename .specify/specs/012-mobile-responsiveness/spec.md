# Spec 012: Mobile Responsiveness & Adaptive Layouts

## 1. Overview
As Genesys21 expands into a multi-platform solution (Android, iOS, WasmJS), the current desktop-first or fixed-width approach in some screens is causing usability issues on smaller devices. This spec aims to standardize responsive design patterns across the project, ensuring a premium experience regardless of screen size.

## 2. Core Objectives
- **Device Agnostic**: Interfaces must fluidly adapt from small smartphones (320dp) to large desktop monitors.
- **Touch-First on Mobile**: Ensure interactive elements meet accessibility standards for touch (minimum 48x48dp targets).
- **Consistent Design System**: Leverage the Atomic Design system to create responsive atoms and molecules.
- **Adaptive Navigation**: Transition between Bottom Navigation (mobile) and Navigation Rails/Drawers (tablet/desktop).

## 3. Responsive Strategy

### 3.1. Adaptive Scaffolding
- Implement `WindowSizeClass` to detect `Compact`, `Medium`, and `Expanded` widths.
- Define standard content max-widths (e.g., 1200dp for desktop) to prevent extreme stretching.

### 3.2. Component-Level Responsiveness
- **Product Cards**: Dynamically adjust column counts in grids (1 column on Compact, 2-3 on Medium, 4+ on Expanded).
- **Search Bars & Filters**: Transition from full-width rows to compact icons or bottom sheets on small screens.
- **Typography**: Scalable font sets that adjust slightly based on the device category.
- **Modals & Dialogs**: Use `BottomSheet` for mobile-native interactions instead of centered dialogs where appropriate.

### 3.3. Specific Fixes (Priority)
- **WhiteLabel Editor**: Improve the floating editor and side panels to be usable on mobile devices.
- **Public Viewer**: Ensure product details and checkout flows are perfectly aligned on narrow screens.
- **Admin Dashboard**: Optimize tables and stats cards to stack vertically on mobile.

## 4. Technical Guidelines
- **BoxWithConstraints**: Use as a primary tool for local component adaptation.
- **FlowRow / FlowColumn**: Replace fixed Rows/Columns for wrapping content like categories and tags.
- **LocalDensity**: Use for precise DP to PX conversions when dealing with complex drawing/animations.
- **Preview Support**: Create @Preview variants for `Phone`, `Tablet`, and `Desktop` for all main templates.

## 5. Implementation Roadmap
1. **Phase 1: Foundation**: Integrate Material3 Window Size Classes and define global responsive tokens.
2. **Phase 2: Core Components**: Refactor `ProductCard`, `GenesysTopAppBar`, and `ServiceCard` for adaptivity.
3. **Phase 3: Screen Templates**: Update `PageComponentRenderer` and `WhiteLabelScreen` to handle compact layouts.
4. **Phase 4: Validation**: Visual regression testing using Paparazzi with multiple device configurations.

## 6. Success Criteria
- [ ] No horizontal scrolling on mobile devices (except for explicitly horizontal components like carousels).
- [ ] All interactive elements have a minimum touch target of 48dp on mobile.
- [ ] Page Editor is fully functional on a standard smartphone screen.
- [ ] 100% of Paparazzi screenshot tests cover at least 3 screen sizes (Small Phone, Tablet, Desktop).
- [ ] Lighthouse Accessibility score > 90 for mobile viewport.
