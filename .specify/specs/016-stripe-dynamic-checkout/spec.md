# Spec 016: Stripe Dynamic Checkout (Payment Element)

## Overview
This specification describes the migration from **Stripe Hosted Checkout** to **Stripe Payment Element**. The primary goal is to provide a seamless "White Label" experience where the payment UI is embedded directly into the Genesys vitrine, inheriting the merchant's chosen theme via `PageThemeConfig`.

## Objectives
- **Branding Consistency**: Embed payment fields directly in the app instead of redirecting to a Stripe-hosted page.
- **Dynamic Theming**: Use Stripe's **Appearance API** to map `PageThemeConfig` colors, shapes, and typography to the payment elements.
- **Reduced Friction**: Keep the user within the Genesys domain during the entire purchase flow.

## Technical Architecture

### 1. Backend (Ktor)
The backend will transition from creating a `CheckoutSession` to creating a `PaymentIntent`.

- **StripeService**:
    - Implement `createPaymentIntent(order: Order, ...): String` which returns the `client_secret`.
    - Continue supporting **Stripe Connect** (Direct Charges) for multi-tenant payouts.
- **Order Flow**:
    - The `POST /api/public/orders` endpoint will now return an `OrderResponse` containing a `clientSecret` instead of a `checkoutUrl`.

### 2. Frontend (Wasm / Compose Multiplatform)
Since the project uses Compose Multiplatform with Wasm, the integration will require a JS bridge.

- **Stripe Bridge**:
    - A JavaScript file (`stripe-bridge.js`) will interface with `@stripe/stripe-js`.
    - It will handle the initialization of the `Stripe` object and the mounting of the `PaymentElement`.
- **Theme Mapper**:
    - A Kotlin utility will translate `PageThemeConfig` (and `CustomThemeConfig`) into a JSON object compatible with Stripe's `Appearance` API.

## Theme Mapping (Appearance API)

The `AppTheme` tokens will be mapped as follows:

| Stripe Variable | Genesys Theme Source |
| :--- | :--- |
| `colorPrimary` | `MaterialTheme.colorScheme.primary` |
| `colorBackground` | `MaterialTheme.colorScheme.surface` |
| `colorText` | `MaterialTheme.colorScheme.onSurface` |
| `colorDanger` | `MaterialTheme.colorScheme.error` |
| `borderRadius` | `customTheme.cornerRadius` (pixels) |
| `fontFamily` | Mapped from `TypographySet` |

### Appearance JSON Example
```json
{
  "theme": "flat",
  "variables": {
    "colorPrimary": "#14213D",
    "colorBackground": "#FFFFFF",
    "colorText": "#14213D",
    "borderRadius": "16px"
  },
  "rules": {
    ".Input": {
      "border": "1px solid #E5E5E5",
      "boxShadow": "none"
    }
  }
}
```

## Payment Flow
1. **Selection**: User adds items to cart and proceeds to checkout.
2. **Intent Creation**: Frontend calls `createOrder`. Backend returns `clientSecret`.
3. **Element Mounting**: Compose UI renders a placeholder where the JS Bridge mounts the `PaymentElement` using the `clientSecret` and computed `Appearance`.
4. **Submission**: User clicks "Pay". The JS Bridge handles the submission to Stripe.
5. **Confirmation**: Stripe confirms the payment. Frontend updates the UI to "Success" and backend receives a webhook (`payment_intent.succeeded`).

## Security & Validation
- **Webhooks**: Must handle `payment_intent.succeeded` to update order status in the database.
- **Validation**: Ensure that `clientSecret` is only sent to the authenticated or valid session customer.
- **Idempotency**: Use `orderId` as an idempotency key where applicable.
