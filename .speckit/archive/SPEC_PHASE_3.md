# Project Specification: Phase 3 - Data Resilience & Insights

## 1. Executive Summary
Phase 3 focuses on stabilizing the core business flow (Checkout) and gaining deep visibility into user behavior. We will implement robust error handling for network operations, optimize image loading performance, and integrate a comprehensive analytics strategy across KMP targets.

## 2. Deep Analytics (FR301)
*   **Unified Logger**: Abstract analytics interface in `shared` to dispatch events to Firebase (Android/iOS) and Custom JS (Web).
*   **Funnel Tracking**: Mandatory tracking for `view_product` -> `add_to_cart` -> `initiate_checkout` -> `purchase_complete`.
*   **Error Monitoring**: Capture network timeouts and serialization failures as non-fatal exceptions in Crashlytics.

## 3. Performance Optimization (FR302)
*   **Image Caching**: Fine-tune Coil 3 memory/disk caching policies for WasmJs to prevent repeated downloads.
*   **Lazy Loading 2.0**: Implement "Predictive Loading" for product details when a user hovers over a product card on Desktop.
*   **Bundle Size**: Reduce Wasm artifact size by optimizing transitive dependencies and ProGuard rules.

## 4. Checkout Resilience (FR303)
*   **Idempotent Orders**: Ensure client-side retry logic doesn't create duplicate orders on the server.
*   **Offline Support**: Store pending cart actions locally using SQLDelight and sync when the connection is restored.
*   **Input Masking**: Premium phone and credit card masks with real-time validation feedback.

## 5. Success Metrics
*   **Checkout Conversion**: Increase completion rate by 15% through better UX feedback.
*   **Load Time**: 90th percentile of main vitrine load < 1.5s on 4G connections.
