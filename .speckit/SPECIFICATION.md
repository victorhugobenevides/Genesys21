# Project Specification: Genesys Premium Layout & Micro-Interactions (Phase 2)

## 1. Executive Summary
This document defines the high-fidelity requirements for Phase 2 of the Genesys21 platform. The goal is to evolve the current functional layouts into a **Premium Design System** characterized by glassmorphism, fluid responsiveness, and dynamic micro-interactions. These improvements will target Android, iOS, and WasmJs to ensure a top-tier e-commerce experience.

## 2. Design Tokens & Visual Principles

### 2.1. Glassmorphism (Frosted Glass)
*   **Surfaces**: Use semi-transparent backgrounds (`Color.White.copy(alpha = 0.7f)`) combined with subtle borders (`0.5.dp`, `Color.White.copy(alpha = 0.2f)`).
*   **Shadows**: Prefer elevation-based tonal color shifts over heavy black shadows to maintain a "light" feel.

### 2.2. Motion & Micro-Interactions
*   **Spring Physics**: All interactive scaling and color transitions MUST use `Spring.DampingRatioMediumBouncy` for a tactile feel.
*   **Staggered Entry**: Lists of products or components should animate items in one by one (staggered) using a 50ms delay per index.
*   **Feedback Loops**: Every primary action (Add to Cart, Save, Delete) MUST have an associated haptic-style visual animation (e.g., morphing icons or scale pulses).

## 3. Functional Requirements (Refined)

### 3.1. Premium Login Screen (FR009)
*   **Animated Gradient**: The background must feature a continuous loop of shifting radial gradients using theme colors.
*   **Frosted Input Card**: The login form must reside in a glassmorphic card with a breathing "Magic" icon (pulsing scale).

### 3.2. Dynamic Product Cards (FR010)
*   **Interactive Scaling**: Cards must scale to `1.03x` on pointer hover or tap hold.
*   **Success Morphing**: The "Add to Cart" button must morph into a green checkmark icon for 800ms upon successful addition.
*   **Category Badge**: A translucent glass badge on the top-left of the image showing the category name.

### 3.3. Dashboard-Style Editor (FR015 - NEW)
*   **Floating Dashboard (Desktop)**: On widths > 1000dp, the editor controls should appear as a "floating" glass card over the preview, rather than a fixed sidebar.
*   **Active Highlighting**: The component being edited or dragged must pulse its border color and transparency.
*   **Skeleton Shimmers**: Image and text blocks must show an animated grey shimmer (`shimmerBrush`) during loading states.

### 3.4. Refined Cart & Checkout (FR012)
*   **Stepper Dots**: Progress indicators that scale and fill color primary as the user moves from "Cart" to "Details" to "Payment".
*   **List Fluidity**: Removing an item from the cart triggers a fade-out followed by a smooth upwards slide of subsequent items.

## 4. Non-Functional Requirements (Refined)

### 4.1. Rendering Integrity (NFR005)
*   Maintain a steady 60 FPS for all animations.
*   Use `Modifier.graphicsLayer` for scale and alpha animations to offload work to the GPU.

### 4.2. Responsive Thresholds (NFR007 - NEW)
*   **Mobile**: < 600dp (Full width, BottomSheets for editors).
*   **Tablet**: 600dp - 1000dp (Grid adjustments, expanded cards).
*   **Desktop**: > 1000dp (Floating Dashboard, split-pane preview).

## 5. User Stories (Expanded)

### US009: Premium Dashboard Feel
*   **As a** Boutique Owner on Desktop,
*   **I want to** see my changes in a large preview while my editor tools float elegantly to the side,
*   **So that** I feel like I'm using a professional design tool.
*   **Acceptance Criteria**:
    - [ ] Editor panel has `Modifier.glassmorphic()` applied.
    - [ ] Editor panel is centered vertically and offset from the right edge.
    - [ ] Transition between Desktop floating and Mobile fixed layout is seamless.

## 6. Success Metrics
*   **UI Satisfaction**: Positive feedback on visual "polish" and haptic-style animations.
*   **Performance**: Zero frame drops during staggered list entries on mid-range Android devices.
