# Implementation Plan: Spec 015 - Security Hardening

## 1. Goal
Hardening Genesys21 against common vulnerabilities and ensuring data privacy.

## 2. Technical Steps

### 2.1 Server Hardening
1. Add `io.ktor:ktor-server-rate-limit` to dependencies.
2. In `Application.kt`, configure a global rate limit (e.g., 100 requests per minute per IP) and a stricter one for `/api/login` (5 per minute).
3. Update `CORS` config to use `allowHost("radarani.site", schemes = listOf("https"))`.
4. Use `install(DefaultHeaders)` to add security headers like `X-Frame-Options: DENY`.

### 2.2 App Hardening
1. Define an expect/actual `SecureStorage` in `shared`.
2. Android: Use `EncryptedSharedPreferences`.
3. iOS: Use `Keychain`.
4. Wasm/JS: Use `localStorage` (limited security) or consider `SessionStorage` for short-lived tokens.
5. In `build.gradle.kts (:composeApp)`, ensure `isMinifyEnabled = true` in the release block.

### 2.3 Secrets Audit
1. Run `grep -rE "AIza|firebase-adminsdk" .` to identify potential leaks.
2. Replace hardcoded Firebase config in `up.sh` and CI with environment variables if possible (though some public keys are necessary for the client).

## 3. Verification
1. Test rate limit by flooding `/api/login` with `curl`.
2. Verify Android APK size and obfuscation using `Analyze APK` in Android Studio.
3. Check Browser console for security header presence using Chrome DevTools.
