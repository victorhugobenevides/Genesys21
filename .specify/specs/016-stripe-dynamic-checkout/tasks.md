# Tasks: Stripe Dynamic Checkout Migration

## Phase 1: Backend (Ktor)
- [ ] **T001** Update Stripe Java SDK to the latest version.
- [ ] **T002** Implement `createPaymentIntent` in `StripeService`.
    - [ ] Map `Order` items to Stripe price/product metadata.
    - [ ] Handle Stripe Connect `stripeAccount` header for direct charges.
- [ ] **T003** Update `OrderResponse` DTO to include `clientSecret` and deprecate `checkoutUrl`.
- [ ] **T004** Update `OrderController` (or equivalent) to handle `PaymentIntent` creation on order placement.
- [ ] **T005** Implement Webhook handler for `payment_intent.succeeded`.
    - [ ] Verify signature.
    - [ ] Update `SqliteOrderRepository` status to `PAID`.

## Phase 2: Frontend (Wasm / Compose Bridge)
- [ ] **T006** Create `stripe-bridge.js` in the Wasm distribution folder.
    - [ ] Method to initialize Stripe with Public Key.
    - [ ] Method to mount `PaymentElement` to a specific DOM ID.
    - [ ] Method to handle payment confirmation and return results to Kotlin.
- [ ] **T007** Implement `StripeThemeMapper.kt` in `shared` or `composeApp`.
    - [ ] Map `PageThemeConfig` to Stripe `Appearance` options.
    - [ ] Handle dark mode transitions.
- [ ] **T008** Create `StripePaymentElement` Composable.
    - [ ] Use `HtmlView` or DOM manipulation to create a target `div`.
    - [ ] Manage loading and error states within the Compose lifecycle.
- [ ] **T009** Update `CheckoutScreen` to use the new embedded payment flow instead of redirection.

## Phase 3: Validation & Testing
- [ ] **T010** Unit test `StripeThemeMapper` for all 21+ `PageThemeConfig` variants.
- [ ] **T011** Integration test: Create an order and verify `clientSecret` generation.
- [ ] **T012** Manual UI validation:
    - [ ] Test with `ROYAL` theme (Light).
    - [ ] Test with `NEON` theme (Dark/Cyberpunk).
    - [ ] Verify responsive layout on mobile screen widths.
- [ ] **T013** Verify webhook processing using Stripe CLI (`stripe listen --forward-to ...`).
