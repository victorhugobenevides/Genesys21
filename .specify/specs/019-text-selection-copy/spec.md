# Spec 019: Text Selection & System Clipboard Integration

## 1. Overview
Currently, many textual elements in Genesys21 (like Order IDs or Product Descriptions) are not selectable. This specification defines how to enable native text selection and clipboard operations across the Design System to improve professional utility for Merchants and convenience for Customers.

## 2. Core Objectives
- **Standardization**: Text selection should be an opt-in property of the `GenesysText` atom.
- **Visual Hygiene**: Interactive elements (Buttons, Menu items) must NOT be selectable to preserve a "native app" feel.
- **Multiplatform Compatibility**: Work with mouse selection (Web/Desktop) and long-press selection (Android/iOS).

## 3. Targeted Elements

### 3.1. High Priority (Must be Selectable)
- **Order Details**: Order ID, Tracking Code, Customer Name, and Customer Phone.
- **Product Details**: Full product description and SKUs.
- **Merchant Settings**: API Keys (with a "Click to Copy" molecule) and Store IDs.

### 3.2. Prohibited Elements (Must NOT be Selectable)
- **Primary/Secondary Buttons**: To avoid selection handles appearing during clicks.
- **Navigation Tabs**: To keep the sidebar/bar feel stable.
- **Status Chips**: To treat them as purely visual indicators.

## 4. Technical Implementation

### 4.1. Design System Update
- **`GenesysText`**: Add `isSelectable: Boolean` parameter.
- **`SelectionContainer`**: Wrap the internal `Text` component with Compose's `SelectionContainer` when `isSelectable` is true.

### 4.2. "Click to Copy" Molecule
- **Component**: `GenesysCopyableText`.
- **UI**: A text label with a small `ContentCopy` icon next to it.
- **Action**: Uses `LocalClipboardManager` to push the value to the system clipboard and shows a "Copied!" snackbar or tooltip.

## 5. Privacy Considerations
- **PII Guarding**: Publicly exposed selectable text should never include non-anonymized PII unless explicitly intended (like the Tracking link for an authenticated user).

## 6. Success Criteria
- [ ] Merchant can copy a 36-character Order UUID with one click/selection.
- [ ] No accidental selection of the "Sair" button label.
- [ ] Seamless clipboard behavior on Mobile (Native context menu) and Web.
