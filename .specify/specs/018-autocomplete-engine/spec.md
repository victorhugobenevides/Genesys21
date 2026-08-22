# Spec 018: Intelligent Autocomplete Engine

## 1. Overview
The **Intelligent Autocomplete Engine** aims to reduce user friction by suggesting relevant data during text input. It targets high-friction areas such as the Product Editor, Order Search, and Checkout forms. The engine must be fast, privacy-aware, and multiplatform.

## 2. Core Objectives
- **Speed**: Suggestions must appear in < 100ms.
- **Context Awareness**: Suggestions for a Merchant must not leak to another Merchant.
- **Fuzzy Matching**: Support partial and typo-tolerant matching.

## 3. Targeted Surfaces

### 3.1. Merchant Administration
- **Product Name**: Suggest names based on existing products in the same `storeId`.
- **Category Name**: Suggest from the global list or the merchant's private list.
- **Search (Orders/Pages)**: Suggest IDs or Customer Names while typing.

### 3.2. Public Viewer & Checkout
- **Location Fields**: Autocomplete for City and State (based on a pre-loaded Brazilian city database).
- **Anonymous Nick**: Suggest common nicks or repeat nicks from the current session.

### 3.3. AI Page Builder
- **Industry/Niche**: Suggest industries (e.g., "Barbearia", "Pet Shop") while the user describes their business to the AI.

## 4. Technical Architecture

### 4.1. Shared Logic (`commonMain`)
- **Fuzzy Filter**: A pure Kotlin implementation of a fuzzy search algorithm (e.g., Levenshtein distance or simple contains-with-weight).
- **Autocomplete State**: A specialized `StateFlow` or `MutableState` that holds the filtered list based on the input buffer.

### 4.2. Design System Atom: `GenesysAutocompleteField`
- **Component**: An extension of `GenesysTextField`.
- **Behavior**: Opens a non-intrusive popup (using `ExposedDropdownMenuBox` or a custom `Popup`) when text is entered.
- **Keyboard Navigation**: Users can select suggestions using arrow keys (Desktop) or touch.

### 4.3. Server Support
- **Unique Name Endpoint**: A lightweight route `/api/suggestions/products?q=...` that returns only unique product names for the authenticated store.

## 5. Security & Privacy
- **Tenancy Isolation**: The server MUST verify `storeId` before returning suggestions to ensure competitive data is not leaked.
- **PII Protection**: Customer names should only be suggested in the **Admin Panel**, never on the Public Viewer (except for the current session's Nick).

## 6. Success Criteria
- [ ] 50% reduction in typing required for repeat product categories.
- [ ] No measurable "jank" in the WasmJS interface during suggestion filtering.
- [ ] Zero data leakage between different merchants.
