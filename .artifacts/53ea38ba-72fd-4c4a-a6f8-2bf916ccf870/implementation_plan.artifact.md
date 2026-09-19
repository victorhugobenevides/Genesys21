# Fix: User Management and SuperAdmin Access 🛡️👥

This plan fixes the issue where the owner cannot manage users in the "Global Users" section. It aligns the server-side security logic with the client-side "GOD MODE" and enhances the user management UI.

## User Review Required

> [!IMPORTANT]
> This change introduces a "GOD MODE" on the server side that automatically promotes the owner (defined by email or UID) to `SUPERADMIN` regardless of the database state. This is necessary to prevent accidental lockouts.

## Proposed Changes

### 1. Server-side Security & Repository (Core Fix)

#### [MODIFY] [SqliteUserRepository.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/repository/SqliteUserRepository.kt)
- Update `ResultRow.toUserProfile()` to implement the "GOD MODE" check.
- Use `System.getenv("OWNER_EMAIL")` or `DogmaConstants.OWNER_EMAIL` to identify the owner.
- Force `role = UserRole.SUPERADMIN` and all permissions for the owner.

### 2. UI Enhancements (Management Capabilities)

#### [MODIFY] [AdminUIComponents.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/components/AdminUIComponents.kt)
- Update `UserActionsSection` to allow `SUPERADMIN` to promote users to all roles (`ADMIN`, `SUPERADMIN`, etc.).
- Add a dropdown or a set of buttons to choose from the full `UserRole` enum if the viewer is a `SUPERADMIN`.

### 3. Stability & Feedback

#### [MODIFY] [GlobalUsersTab.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/list/tabs/GlobalUsersTab.kt)
- Ensure the current user's role is checked before rendering management actions.
- Improve error handling for 403 Forbidden responses.

## Verification Plan

### Automated Tests
- Run `PageViewModelTest` to ensure role management methods still work.

### Manual Verification
- Log in as the owner ("victorkoto@gmail.com").
- Navigate to "Usuários Global".
- Verify that the list of users is correctly loaded (no 403 error).
- Verify that you can change a user's role and permissions.
