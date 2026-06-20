# Project Specification: Phase 5 - Advanced Customization & Pro Themes

## 1. Executive Summary
Phase 5 aims to provide merchants with professional-grade design tools. Instead of choosing from fixed themes, users will be able to customize specific brand colors, select from curated typography sets, and adjust component-level styling (corner radius, glass intensity).

## 2. Dynamic Theme Engine (FR501)
*   **Custom Palette**: Allow merchants to override `Primary`, `Secondary`, and `Surface` colors.
*   **Typography Sets**: Curated Google Fonts integration (Mobile & Web) with presets like "Modern Sans", "Classic Serif", and "Minimal Mono".
*   **Style Persistence**: Store the custom theme overrides in the `Page` model and persist them via SQLDelight (server) and LocalStorage (client).

## 3. Pro Component Styling (FR502)
*   **Glass Intensity**: A slider to control the blur and transparency level of glassmorphic elements.
*   **Rounding Config**: Global setting for corner radius (Sharp, Medium, Rounded, Full).
*   **Header Hero Layouts**: New layouts for `ProfileHeader` (Full-width banner, Centered Overlay, Split Screen).

## 4. Merchant Preview 2.0 (FR503)
*   **Real-time Lab**: Improve the WhiteLabel editor to reflect style changes instantly without page reload.
*   **Theme Presets**: Create 5 new "Pro" templates that use these advanced styling features.

## 5. Success Metrics
*   **Merchant Satisfaction**: Increase customization score in internal tests.
*   **Brand Uniqueness**: Ensure that no two stores look identical even with the same template.
