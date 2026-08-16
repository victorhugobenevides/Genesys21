# Tasks: Spec 015 - Security Hardening

## Phase 1: Server-Side Hardening (Ktor)
- [x] Install and configure Ktor `RateLimit` plugin. <!-- id: 0 -->
- [x] Restrict CORS to specific production and staging domains. <!-- id: 1 -->
- [x] Implement `DefaultHeaders` with `X-Frame-Options`, `X-Content-Type-Options`, and `HSTS`. <!-- id: 2 -->
- [x] Sanitize `StatusPages` error responses to avoid leaking stack traces/messages in production. <!-- id: 3 -->
- [x] Implement maximum file size and type validation in `/api/upload`. <!-- id: 13 -->

## Phase 2: Database & Multi-tenancy Hardening
- [x] Enforce `ownerId` checks in all `SqlitePageRepository` write operations (Categories, Products). <!-- id: 14 -->
- [x] Integrate `AuditLogsTable` into critical repository operations (Role updates, Page deletions). <!-- id: 15 -->
- [x] Transition from sequential integer IDs to UUIDs for `PageComponents` (Spec 011 alignment). <!-- id: 16 -->

## Phase 3: App-Side (Compose)
- [ ] Implement `SecureStorage` interface with platform-specific implementations. <!-- id: 4 -->
- [ ] Migrate `AuthRepository` token storage to `SecureStorage`. <!-- id: 5 -->
- [x] Add R8/ProGuard rules for `composeApp` production build. <!-- id: 6 -->
- [x] Implement `VisualTransformation` for all sensitive inputs (Login/Password). <!-- id: 7 -->

## Phase 4: Infrastructure & Quality
- [ ] Add a `check-secrets` script to the pipeline to scan for hardcoded keys. <!-- id: 8 -->
- [ ] Setup Dependabot in the GitHub repository. <!-- id: 9 -->
- [x] Implement user data export/deletion endpoints (Compliance LGPD). <!-- id: 10 -->

## Phase 5: Audit & Logging
- [x] Create a centralized `AuditLogger` in the server. <!-- id: 11 -->
- [x] Connect `AuditLogger` to sensitive operations (Role changes, Store deletion, Order cancellation). <!-- id: 12 -->
