# Public Viewer Specification

## Overview
The Public Viewer is the face of the Genesys21 platform. It renders the pages created by merchants for end-visitors. It must be fast, SEO-friendly (where possible), and highly responsive across all devices.

## Core Features

### 1. Dynamic Rendering
- Fetches page structure from `PageRepository.getPublicPage(id)`.
- Uses `PageComponentRenderer` to transform JSON components into Compose UI.
- Supports deep linking: `/p/{id}` on Mobile and direct URL pathing on Web.

### 2. Search & Filter
- Real-time filtering of product lists within components.
- Category-based navigation using the `CategoryFilter` component.

### 3. Shopping Experience
- Product cards with quick "Add to Cart" actions.
- Product details view with image carousel and description.
- Integrated cart drawer/screen for checkout flow.

### 4. Custom Branding
- Renders page with the merchant's selected `PageThemeConfig`.
- Supports custom domains (mapped on the server via `PageRepository.getPageByDomain(domain)`).

## Technical Constraints
- **Web (WasmJS)**: Must handle canvas rendering and scroll event passthrough.
- **Mobile**: Native integration for sharing and WhatsApp contact.
- **Server**: Must provide SEO metadata for the social preview (OpenGraph/Twitter Cards).

## Acceptance Criteria
- Page loads in under 1.5s on average network conditions.
- Images are lazy-loaded and optimized for the device resolution.
- The cart count is preserved when navigating between the viewer and product details.
- "Buy on WhatsApp" button correctly formats the message with cart items.
