# Spec: Premium Layout & Micro-Interactions (Genesys Design System v2)

This specification defines the layout, responsiveness, and micro-interaction improvements for the Genesys21 platform. The goal is to evolve the current layout into a highly aesthetic, responsive, and dynamic experience utilizing modern design patterns such as glassmorphism, smooth gradients, and interactive animations.

## Principles & Design Tokens
*   **Aesthetic (Glassmorphism)**: Use translucent surfaces (`Color.White.copy(alpha = 0.7f)`) with backdrop blur (or drop shadow approximations in Compose) and thin borders (`0.5.dp`, `Color.White.copy(alpha = 0.2f)`).
*   **Dynamic (Micro-Animations)**: Scale transitions, spring physics (`Spring.DampingRatioMediumBouncy`), and animated state transitions (e.g., loading overlays, button states).
*   **Responsive (Multi-Device Layouts)**: Fluid grid adjustments based on local screen width boundaries, with dedicated desktop/tablet optimizations.

---

## User Stories

- **As a** Store Customer seeking a modern login experience, I want the Login Screen to feature an animated gradient background and a glassmorphic input card, so that my first impression of the brand is premium.
- **As a** Store Customer browsing products in the vitrine, I want the Product Cards to scale smoothly on hover and feature dynamic tag badges, so that searching for products feels interactive and alive.
- **As a** Store Customer navigating product categories, I want the Category Chips to morph background colors and slide dynamically when selected, so that filtering feels instant and satisfying.
- **As a** Store Customer reviewing my cart, I want the Cart Screen to include a glassmorphic summary sidebar and animated page-transition steps, so that checking out is clear and engaging.
- **As a** Store Customer checking product details, I want the Product Details Screen to offer a responsive split-pane desktop view and a smooth image-carousel with scaling indicator dots, so that I can inspect products with ease.
- **As a** Boutique Owner designing my page, I want the WhiteLabel Editor to support skeleton loaders and pulsing reorder handles during drag operations, so that structuring my page is visually feedback-rich and effortless.

