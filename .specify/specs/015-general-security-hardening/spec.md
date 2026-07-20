# Spec 015: General Security Hardening (Server & App)

## 1. Overview
As Genesys21 transitions from a prototype to a production-ready white-label platform, it is critical to implement industry-standard security measures. This specification outlines the hardening of both the Kotlin Ktor server and the Compose Multiplatform app to protect user data, prevent unauthorized access, and ensure system integrity.

## 2. Server-Side Hardening (Ktor)

### 2.1 API Security
- **Rate Limiting**: Implement the `RateLimit` plugin in Ktor to prevent brute-force and DDoS attacks on sensitive endpoints (`/api/login`, `/api/upload`).
- **CORS Policy**: Restrict Cross-Origin Resource Sharing (CORS) to specific allowed domains (e.g., `radarani.site`, `victorbenevides.dev`).
- **Input Validation**: Strict validation of all incoming payloads using `kotlinx.serialization` and custom validators to prevent injection and malformed data.
- **Security Headers**: Configure standard security headers (HSTS, Content-Security-Policy, X-Frame-Options, X-Content-Type-Options).

### 2.2 Data Protection
- **Environment Secrets**: Ensure NO secrets (Firebase keys, Database passwords) are hardcoded. Use `System.getenv()` or encrypted `.env` files.
- **Encryption at Rest**: Evaluate SQLite's SQLCipher or similar for encrypting sensitive tenant data in the database.
- **UUID Mapping**: Mask internal sequential IDs with UUIDs for all public-facing URLs (Spec 011).

### 2.3 Networking
- **HTTPS Enforcement**: Force redirection from HTTP to HTTPS (already partially implemented in Nginx).
- **Secure WebSockets**: If used, ensure `wss://` is enforced.

## 3. App-Side Hardening (Compose Multiplatform)

### 3.1 Secure Storage
- **Token Management**: Store Firebase ID Tokens and sensitive session data using platform-specific secure storage (EncryptedSharedPreferences on Android, Keychain on iOS).
- **Draft Protection**: Ensure page drafts stored locally are cleared upon sign-out.

### 3.2 Obfuscation & Integrity
- **R8/ProGuard**: Configure R8 to obfuscate the Android production build, making reverse engineering more difficult.
- **Root/Jailbreak Detection**: (Optional for V1) Consider basic checks to warn users if the app is running on a compromised device.

### 3.3 UI Security
- **Sensitive Data Masking**: Use `VisualTransformation` in TextFields for passwords and credit card info (Spec 014).
- **Auth Guard**: Re-verify token validity before performing high-privilege actions in the Editor.

## 4. Infrastructure & CI/CD Security

### 4.1 Secrets Management
- **CircleCI/GitHub Secrets**: Centralize all deployment keys (OCI_SSH_KEY, Firebase JSON) in the CI provider's vault.
- **Log Masking**: Ensure CI logs do not print environment variables or private keys.

### 4.2 Dependency Scanning
- **Dependabot/Snyk**: Enable automated scanning for vulnerable libraries in `libs.versions.toml`.

## 5. Compliance (LGPD/GDPR)
- **Data Deletion**: Implement a "Delete Account" flow that removes all user-related data (or anonymizes it).
- **Audit Logs**: Every sensitive state change (Role update, Order cancellation) must be logged with `userId` and `timestamp`.

## 6. Success Criteria
- [ ] Rate limiting functional on login endpoints.
- [ ] Production build uses R8 obfuscation.
- [ ] All API responses have correct security headers.
- [ ] No secrets found in code via static analysis (grep).
- [ ] LGPD-compliant data deletion endpoint implemented.
- [ ] CI/CD pipeline completes without exposing keys in logs.
