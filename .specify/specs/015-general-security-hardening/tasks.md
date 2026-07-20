# Tasks: Spec 015 - Security Hardening

## Phase 1: Server-Side (Ktor)
- [ ] Install and configure Ktor `RateLimit` plugin. <!-- id: 0 -->
- [ ] Refactor CORS configuration to use a whitelist of domains. <!-- id: 1 -->
- [ ] Add security headers middleware to Ktor. <!-- id: 2 -->
- [ ] Audit `SqlitePageRepository` for potential SQL injection points (even when using Exposed). <!-- id: 3 -->

## Phase 2: App-Side (Compose)
- [ ] Implement `SecureStorage` interface with platform-specific implementations. <!-- id: 4 -->
- [ ] Migrate `AuthRepository` token storage to `SecureStorage`. <!-- id: 5 -->
- [ ] Add R8/ProGuard rules for `composeApp` production build. <!-- id: 6 -->
- [ ] Implement `VisualTransformation` for all sensitive inputs. <!-- id: 7 -->

## Phase 3: Infrastructure & Quality
- [ ] Add a `check-secrets` script to the pipeline to scan for hardcoded keys. <!-- id: 8 -->
- [ ] Setup Dependabot in the GitHub repository. <!-- id: 9 -->
- [ ] Implement user data export/deletion endpoints (Compliance). <!-- id: 10 -->

## Phase 4: Audit & Logging
- [ ] Create a centralized `AuditLogger` in the server. <!-- id: 11 -->
- [ ] Connect `AuditLogger` to sensitive operations (Role changes, Store deletion). <!-- id: 12 -->
