# Project Specification: Phase 4 - Social & SEO (Growth Wave)

## 1. Executive Summary
Phase 4 focuses on turning Genesys21 pages into viral marketing tools. By optimizing how links appear on social media and making it easy for customers to share pages natively, we aim to increase organic traffic and improve merchant visibility on search engines.

## 2. Dynamic SEO & Rich Previews (FR401)
*   **Dynamic OpenGraph**: The server must fetch the Page's title, first image, and bio to populate `og:title`, `og:image`, and `og:description` meta tags.
*   **Wasm SEO Passthrough**: Ensure that web crawlers (Googlebot) see the static metadata served by Ktor before the Wasm app hydrates.
*   **Automated Sitemap**: Implement a `/sitemap.xml` route on the server that lists all active public page IDs.

## 3. Native Social Integration (FR402)
*   **Multiplatform Share API**: Implement a "Share" button in the Viewer top bar that uses `UIActivityViewController` (iOS), `Intent.ACTION_SEND` (Android), and `navigator.share` (Web).
*   **WhatsApp Contextual Messaging**: When a customer clicks "Contact" from a product card, the WhatsApp message should pre-fill with: *"Olá! Vi o produto [Nome] na sua vitrine e gostaria de mais informações."*
*   **Favicon Branding**: If a merchant has a `ProfileHeader` image, use that image (scaled) as the browser's dynamic favicon.

## 4. Discovery & Analytics (FR403)
*   **Referrer Tracking**: Log the source of traffic (e.g., `utm_source=instagram`) in our new unified Analytics engine.
*   **Viral Index**: Measure how many times the "Share" button is clicked vs. completed purchases.

## 5. Success Metrics
*   **Social CTR**: Increase click-through rate of shared links by 40% using rich previews.
*   **Organic Discovery**: Have at least 20% of new traffic originating from search engines within 3 months.
