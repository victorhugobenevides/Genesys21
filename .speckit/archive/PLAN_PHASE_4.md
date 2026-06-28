# Implementation Plan: Phase 4 - Social & SEO

## Architecture Overview
Most changes will occur in the `server` (metadata rendering) and `composeApp` (Native Share implementation). The `shared` module will host the `ShareManager` interface.

## Task Breakdown

### Wave 1: SEO Hardening (Server-side)
**T022: Dynamic Metadata Engine**
- **Dependencies**: None | **Est**: 4h
- **Description**: Update Ktor `/p/{id}` route to parse page components and inject real images and bios into HTML template.

**T023: Sitemap & Search Discovery**
- **Dependencies**: T022 | **Est**: 2h
- **Description**: Create `/sitemap.xml` endpoint for automated indexing.

### Wave 2: Social Kit (Client-side)
**T024: Native Share Integration**
- **Dependencies**: None | **Est**: 5h
- **Description**: Create `expect/actual` `ShareManager` for Android, iOS, and Web. Add Share icon to `PageViewerScreen` top bar.

**T025: Contextual WhatsApp Feedback**
- **Dependencies**: None | **Est**: 2h
- **Description**: Update WhatsApp link generators to include product-specific context strings.

### Wave 3: Advanced Branding
**T026: Dynamic Favicon & UTM Tracking**
- **Dependencies**: T024 | **Est**: 3h
- **Description**: Inject favicon links via JS Interop and capture `utm_` parameters in Analytics.

---\n
## Execution waves

### Wave 1 (SEO - 6h)
- T022, T023

### Wave 2 (Social - 7h)
- T024, T025

### Wave 3 (Branding - 3h)
- T026

**Total Estimated Time**: 16 hours
