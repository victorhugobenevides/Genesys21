# Project Specification: WhiteLabel Editor & Multiplatform Viewer

## Overview
This document defines the requirements for the WhiteLabel module and the Genesys21 Layout Refinement (Phase 2). This module enables boutique owners to build dynamic pages ("vitrines") using a drag-and-drop-style component system. Phase 2 introduces high-fidelity glassmorphism, responsive reflows, and rich micro-interactions across Android, iOS, and WasmJs.

## Functional Requirements

### FR001: Component Variety
**Description**: Support for Header, Text, Image, ProductList, ProfileHeader, SocialLinks, Button, Filter, and CategoryFilter.
**Priority**: High
**Testability**: Verify component classes exist and map correctly to renderers.

### FR002: Strict Serialization
**Description**: The system must handle unknown components gracefully using an Unknown data class fallback.
**Priority**: High
**Testability**: Test serialization/deserialization with unknown component tags.

### FR003: Data Integrity
**Description**: Every component modification must update the Page object in the WhiteLabelState immediately.
**Priority**: High
**Testability**: Assert UI state updates on component modifications.

### FR004: Component Management
**Description**: Add, remove, and reorder (Move Up/Down) components.
**Priority**: High
**Testability**: Test state mutation after add, delete, and move events.

### FR005: Live Preview
**Description**: Real-time rendering of the page theme and layout as changes occur.
**Priority**: High
**Testability**: Verify theme changes propagate to Compose preview layouts.

### FR006: Draft Lifecycle
**Description**: Auto-save to PageDraftRepository on every change; manual Publish action; Discard Draft option.
**Priority**: High
**Testability**: Verify draft record persistence in local DB on each state change.

### FR007: Specialized Editors
**Description**: ProfileHeaderEditor, ProductListEditor, and ImageEditor with local upload support.
**Priority**: High
**Testability**: Verify each editor screen captures input and mutates parent components.

### FR008: Responsive Layout
**Description**: Single-column bottom-sheet editor on Mobile (<1000dp); split-pane (65%/35%) on Desktop (>1000dp).
**Priority**: High
**Testability**: Verify layout changes when local container width crosses 1000dp threshold.

### FR009: Premium Glassmorphic Login
**Description**: The LoginScreen must feature a dynamic animated radial gradient background and a translucent input card container.
**Priority**: High
**Testability**: Assert card transparency styling and gradient animation loops exist.

### FR010: Interactive Product Cards
**Description**: ProductCard components must scale up smoothly on pointer hover/tap, display dynamic stock status badges, and show spring-loaded Add to Cart confirmations.
**Priority**: High
**Testability**: Test card scale modifier value reaches 1.03f and button morphs to checkmark.

### FR011: Morphing Category Chips
**Description**: Category filter chips must morph background colors and elevations with animated transition transitions.
**Priority**: Medium
**Testability**: Verify color state transitions from surface variant to primary container.

### FR012: Dynamic Cart & Stepper UI
**Description**: The CartScreen must feature a glassmorphic summary panel and step indicator animations.
**Priority**: Medium
**Testability**: Assert cart stepper dots scale on page steps and list deletions fade out.

### FR013: Snapping Carousel Indicators
**Description**: Image carousels on ProductDetailsScreen must use page-snapping behavior and show size-scaling active page indicator dots.
**Priority**: Medium
**Testability**: Verify Active Dot scale increases to 1.5x of base inactive dots.

### FR014: Editor Skeleton & Handles
**Description**: The WhiteLabelScreen reordering handles must pulse visually when held, and components must display skeleton layouts during load states.
**Priority**: Medium
**Testability**: Assert active reorder component border alpha oscillates.

## Non-Functional Requirements

### NFR001: Build Reliability
**Description**: Shared code (commonMain) must compile for Android, iOS, and WasmJs without platform-specific leaks.
**Metric**: Compilation success status
**Target**: 100% compile pass across all 3 compilation targets.

### NFR002: Rendering Performance
**Description**: Use stable key values in LazyColumn and Modifier.animateItem for smooth reordering.
**Metric**: Reordering animation frame rate
**Target**: Maintain 60 FPS target on UI reorder thread.

### NFR003: Type Safety
**Description**: Expect/Actual functions must have identical signatures across platforms.
**Metric**: Compilation link success
**Target**: 100% check pass on link boundaries.

### NFR004: Zero-Loss Drafts
**Description**: Drafts must persist across process death (Android) or page refresh (WasmJs).
**Metric**: Persistence query success
**Target**: Recover 100% of uncommitted state after app termination.

### NFR005: Micro-Interaction Frame Rate
**Description**: All animated UI transitions must target 60 frames per second (FPS) rendering on test devices.
**Metric**: Render frame latency
**Target**: Frame draw time under 16ms.

### NFR006: Layout Shift Mitigation
**Description**: Component reflow must use pre-calculated space constraints to achieve a Cumulative Layout Shift (CLS) of 0.
**Metric**: Cumulative Layout Shift
**Target**: CLS score of 0.

## User Stories

### US001: Boutique Branding
**As a** Boutique Owner  
**I want to** personalize my page with a profile picture and bio  
**So that** my brand identity is instantly recognizable.  
**Acceptance Criteria**:
- [x] Clicking the profile image opens the native image picker.
- [ ] Image is uploaded to the server and a URL is returned.
- [ ] Preview updates immediately without refreshing the entire screen.

### US002: Fast Shopping Experience
**As a** Customer  
**I want to** browse a stable list of products  
**So that** my scroll position when images load is preserved.  
**Acceptance Criteria**:
- [x] Items use stable keys for lazy list state restoration.
- [ ] Category filters apply instantly to the ProductList.

### US003: Premium Login Experience
**As a** Store Customer seeking a modern login experience  
**I want the** Login Screen to feature an animated gradient background and a glassmorphic input card  
**So that** my first impression of the brand is premium.  
**Acceptance Criteria**:
- [ ] Login card has a border of 0.5.dp with opacity Color.White.copy(alpha = 0.2f) and frosted background.
- [ ] Background displays a shifting radial gradient moving across primary and background theme colors.
- [ ] Magic login icon has a breathing scale animation pulsing between 0.95f and 1.05f every 2.5s.

### US004: Dynamic Product Card
**As a** Store Customer browsing products in the vitrine  
**I want the** Product Cards to scale smoothly on hover and feature dynamic tag badges  
**So that** searching for products feels interactive and alive.  
**Acceptance Criteria**:
- [ ] Card scales up to 1.03f on cursor hover or tap hold using spring damping.
- [ ] Esgotado badge scales in with slide-up fade-in transition when product stock is 0.
- [ ] Add to Cart button morphs into a green checkmark check for 800ms when clicked.

### US005: Category Filter Chips Transition
**As a** Store Customer navigating product categories  
**I want the** Category Chips to morph background colors and slide dynamically when selected  
**So that** filtering feels instant and satisfying.  
**Acceptance Criteria**:
- [ ] Selected chip animate-color morphs background from surface variant to primary container color.
- [ ] Selecting a chip slides the neighboring chips smoothly into their new positions.

### US006: Cart & Stepper Animations
**As a** Store Customer reviewing my cart  
**I want the** Cart Screen to include a glassmorphic summary sidebar and animated page-transition steps  
**So that** checking out is clear and engaging.  
**Acceptance Criteria**:
- [ ] Wide screen summary panel features frosted white layout with subtle drop shadow.
- [ ] Cart stepper indicator dots scale up and fill color primary dynamically when the user advances steps.
- [ ] Items removed from cart fade out while other items animate upwards.

### US007: Product Detail Snap Carousel
**As a** Store Customer checking product details  
**I want the** Product Details Screen to offer a responsive split-pane desktop view and a smooth image-carousel with scaling indicator dots  
**So that** I can inspect products with ease.  
**Acceptance Criteria**:
- [ ] Desktop layout shows 50% left-pane carousel and 50% right-pane info card.
- [ ] Carousel uses HorizontalPager with snappy drag release page snap.
- [ ] Page indicator dots scale their radius by 1.5x when active and dim inactive dots.

### US008: Editor Skeleton & Pulsing Handles
**As a** Boutique Owner designing my page  
**I want the** WhiteLabel Editor to support skeleton loaders and pulsing reorder handles during drag operations  
**So that** structuring my page is visually feedback-rich and effortless.  
**Acceptance Criteria**:
- [ ] Active dragging component displays a pulsing semi-transparent border highlighting its select state.
- [ ] Pulse animation cycles alpha between 0.3f and 0.8f at 1.5s frequency.
- [ ] Skeletons display grey animated shimmers for images/text blocks during server fetches or uploads.

## Constraints
- Shared code must compile for Android, iOS, and WasmJs.
- Sensitive credentials must never be committed.

## Success Metrics
- 100% build pass on CircleCI for Android and WasmJs.
- Recover 100% of uncommitted state after app termination.
