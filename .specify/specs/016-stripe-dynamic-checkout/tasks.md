# Tasks: Stripe Dynamic Checkout Migration

## Phase 1: Backend (Ktor)
- [x] **T001** Update Stripe Java SDK to the latest version.
- [x] **T002** Implement `createPaymentIntent` in `StripeService`.
    - [x] Map `Order` items to Stripe price/product metadata.
    - [x] Handle Stripe Connect `stripeAccount` header for direct charges.
- [x] **T003** Update `OrderResponse` DTO to include `clientSecret` and deprecate `checkoutUrl`.
- [x] **T004** Update `OrderController` (or equivalent) to handle `PaymentIntent` creation on order placement.
- [x] **T005** Implement Webhook handler for `payment_intent.succeeded`.
    - [x] Verify signature.
    - [x] Update `SqliteOrderRepository` status to `PROCESSING`.

## Phase 2: Frontend (Wasm / Compose Bridge)
- [x] **T006** Create `stripe-bridge.js` in the Wasm distribution folder.
    - [x] Method to initialize Stripe with Public Key.
    - [x] Method to mount `PaymentElement` to a specific DOM ID.
    - [x] Method to handle payment confirmation and return results to Kotlin.
- [x] **T007** Implement `StripeThemeMapper.kt` in `shared` or `composeApp`.
    - [x] Map `PageThemeConfig` to Stripe `Appearance` options.
    - [x] Handle dark mode transitions.
- [x] **T008** Create `StripePaymentElement` Composable.
    - [x] Use `HtmlView` or DOM manipulation to create a target `div`.
    - [x] Manage loading and error states within the Compose lifecycle.
- [x] **T009** Update `CheckoutScreen` (CartScreen) to use the new embedded payment flow instead of redirection.

## Phase 3: Validation & Testing
- [x] **T010** Unit test `StripeThemeMapper` for all 21+ `PageThemeConfig` variants.
- [x] **T011** Integration test: Create an order and verify `clientSecret` generation.
- [x] **T012** Manual UI validation:
    - [x] Test with `ROYAL` theme (Light).
    - [x] Test with `NEON` theme (Dark/Cyberpunk).
    - [x] Verify responsive layout on mobile screen widths (Paparazzi).
- [x] **T013** Verify webhook processing using Stripe CLI (`stripe listen --forward-to ...`).
