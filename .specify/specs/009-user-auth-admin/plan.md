# Implementation Plan - User Auth & SuperAdmin

## Phase 1: Server-Side RBAC & Data Model
1. [ ] Update `UsersTable` in server to include `role` (String) and `approved` (Boolean).
2. [ ] Hardcode the check for `victorkoto@gmail.com` to always be `SUPERADMIN` during the first migration.
3. [ ] Create `AdminRoutes.kt` with endpoints protected by a custom `SuperAdminAuthenticator`.

## Phase 2: Shared Logic & UseCases
1. [ ] Define `UserRole` enum in `shared`.
2. [ ] Update `AuthRepository` to support `signInWithGoogle(idToken)`.
3. [ ] Create `GetUserRoleUseCase` to determine the UI layout on login.
4. [ ] Update `PageRepository` to enforce `isApproved` check before allowing page creation.

## Phase 3: Login & SuperAdmin Dashboard (Client)
1. [ ] Integrate Google Login button in `LoginScreen` using `kmpauth`.
2. [ ] Add `SuperAdminTab` to the Merchant Dashboard.
3. [ ] Create a `UserList` component with searchable names/emails.
4. [ ] Implement a `GrantAccessDialog` to toggle the `MERCHANT` role.

## Phase 4: Forced Auth for Customers
1. [ ] Wrap `CartScreen` checkout logic in an `AuthCheck`.
2. [ ] Update `ServiceBookingScreen` to show a "Login to Book" button instead of "Confirm" for guest users.
3. [ ] Link `Appointment` and `Order` objects to the registered `userId` instead of just name/phone.
