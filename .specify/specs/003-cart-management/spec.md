# Cart Management Specification

## Overview
The Cart Management system handles the lifecycle of items selected by a visitor across all platform targets (Web, Android, iOS). It must remain consistent whether the user is authenticated or a guest.

## Core Features

### 1. Item Persistence
- Items must persist locally using target-specific storage (e.g., `window.localStorage` on WasmJs).
- Items include: `Product` reference and `quantity`.

### 2. Session ID Handling
- Guest users are identified by a unique `sessionId`.
- Session IDs must be stable across browser refreshes or app restarts.

### 3. Server Synchronization
- The cart should automatically sync with the backend (`/api/cart`).
- Authentication status changes must trigger a merge of local guest items into the user's account.

### 4. Operations
- `addToCart(item)`: Adds a new product or increments quantity of existing.
- `removeFromCart(productId)`: Removes all quantities of a product.
- `updateQuantity(productId, quantity)`: Updates to specific amount; removes if <= 0.
- `clearCart()`: Empties all items.

## Models
- **CartItem**: `product: Product`, `quantity: Int`.
- **CartRepository**: Interface defining the above operations.

## Acceptance Criteria
- Adding a product updates the total price instantly.
- The cart state is reactive and shared across different parts of the UI.
- Reloading the page/app does not empty the cart.
- Syncing with the server handles network errors gracefully (retries or offline mode).
