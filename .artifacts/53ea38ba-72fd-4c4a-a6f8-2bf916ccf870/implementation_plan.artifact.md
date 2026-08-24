# Fix Persistent ClassCastException in ScreensSnapshotTest

The previous attempt to fix the `ClassCastException` in `ScreensSnapshotTest.kt` using reflection-based coercion failed. The exception persists at the boundary where the `UserProfile` object crosses classloaders between the test execution and the Paparazzi rendering environment.

## Analysis

The `ClassCastException` occurs because even if the parameter is `Any?`, the runtime still encounters issues when the code inside the rendering block tries to treat the object as a `UserProfile` from its own classloader, while it was instantiated in the test classloader.

The most reliable way to fix this in Paparazzi tests is to **avoid passing project-specific class instances across the classloader boundary**. Instead, we should pass primitive types (Strings, Ints, etc.) and reconstruct the necessary objects inside the rendering block.

## Proposed Changes

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Update `genesysSnapshot` and `genesysResponsiveSnapshot` to accept primitive mock parameters instead of a `UserProfile` object:
    - `mockUserId: String? = null`
    - `mockUserRole: String? = null`
    - `mockUserPermissions: List<String>? = null`
- Inside the `snapshot` block, reconstruct the `UserProfile` object using these primitives.
- Remove the `coerceUserProfile` function.

#### [MODIFY] [ScreensSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/ScreensSnapshotTest.kt)
- Update `testAdminDashboardResponsive` to pass individual strings for ID, Role, and Permissions instead of the `sampleSuperAdmin` object.

## Verification Plan

### Automated Tests
- Since local Gradle execution is failing due to environment issues, I will verify the logic by ensuring no `UserProfile` instances are passed across the `snapshot` boundary.
- The user should run `:screenshot-tests:testDebugUnitTest --tests "com.itbenevides.genesys21.screenshot.ScreensSnapshotTest.testAdminDashboardResponsive"` to verify the fix in their environment.
