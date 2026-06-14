# Project Specification: Genesys Premium Layout & Micro-Interactions (Phase 2)

## 1. Executive Summary
This document defines the high-fidelity requirements for Phase 2 of the Genesys21 platform. The goal is to evolve the current functional layouts into a **Premium Design System** characterized by glassmorphism, fluid responsiveness, and dynamic micro-interactions. These improvements target Android, iOS, and WasmJs to ensure a top-tier e-commerce experience.

## 2. Design Tokens & Visual Principles

### 2.1. Glassmorphism (Frosted Glass)
*   **Surfaces**: Semi-transparent backgrounds (`Color.White.copy(alpha = 0.7f)` for light mode).
*   **Borders**: Subtle hair-line borders (`0.5.dp`, `Color.White.copy(alpha = 0.2f)`).
*   **Blur**: Utilize `Modifier.blur(20.dp)` where supported (native/Wasm targets).
*   **Shadows**: Prefer tonal elevation color shifts over heavy black shadows to maintain a "light" feel.

### 2.2. Motion & Micro-Interactions
*   **Physics**: `Spring.DampingRatioMediumBouncy` for all interactive scaling and transitions.
*   **Staggered Entry**: Lists of products or components animate sequentially with a **50ms offset** per index.
*   **Feedback Loops**: Every primary action (Add to Cart, Save, Delete) MUST have an associated haptic-style visual animation (e.g., morphing icons or scale pulses).

## 3. Functional Requirements

### 3.1. Premium Login Screen (FR009)
*   **Animated Gradient**: The background must feature a continuous loop of shifting radial gradients using theme colors.
*   **Frosted Input Card**: The login form must reside in a glassmorphic card with a breathing "Magic" icon (pulsing scale).

### 3.2. Dynamic Product Cards (FR010)
*   **Interactive Scaling**: Cards must scale up to `1.03x` on pointer hover or tap hold using spring physics.
*   **Success Morphing**: The "Add to Cart" button must morph into a green checkmark icon for 800ms upon successful addition.
*   **Category Badge**: A translucent glass badge overlay on the top-left of the image.

### 3.3. Dashboard-Style Editor (FR015)
*   **Floating Dashboard (Desktop)**: On widths > 1000dp, the editor controls should appear as a "floating" glass card offset from the screen edge.
*   **Active Highlighting**: Edited components pulse their border alpha between `0.3f` and `0.8f`.
*   **Skeleton Shimmers**: Image and text blocks must show an animated grey shimmer (`shimmerBrush`) during loading states.

### 3.4. Refined Cart & Checkout (FR012)
*   **Stepper Dots**: Progress indicators that scale by `1.5x` and fill color primary as the user advances.
*   **List Fluidity**: Removing an item from the cart triggers a fade-out followed by a smooth upwards slide of subsequent items.

## 4. Non-Functional Requirements

### 4.1. Rendering Integrity (NFR005)
*   **Performance**: Maintain a steady 60 FPS for all animations. Use `Modifier.graphicsLayer` to minimize main-thread recomposition work.
*   **Responsiveness**:
    *   **Mobile (< 600dp)**: Single width, BottomSheet editors.
    *   **Tablet (600-1000dp)**: Adaptive grids, expanded info cards.
    *   **Desktop (> 1000dp)**: Floating Dashboard, split-pane active preview.

## 5. User Stories (Expanded)

### US009: Premium Dashboard Feel
*   **As a** Boutique Owner on Desktop, **I want** to see my changes in a large preview while my editor tools float elegantly to the side, **so that** I feel like I'm using a professional design tool.
*   **Acceptance Criteria**:
    - [ ] Editor panel has `Modifier.glassmorphic()` applied.
    - [ ] Editor panel is centered vertically and offset from the right edge.
    - [ ] Transition between Desktop floating and Mobile fixed layout is seamless.

## 6. Success Metrics
*   **UI Satisfaction**: Positive qualitative feedback on visual "polish" and snappiness.
*   **Multi-Platform Parity**: 100% visual consistency between Android and WasmJs browser targets.
