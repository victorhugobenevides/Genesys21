# Final Resilient Solution for ClassCastException in Paparazzi Tests

The `ClassCastException` persists because of complex object passing and variable capturing across the Paparazzi/LayoutLib classloader boundary.

## Analysis

Even with `inline` functions and primitive parameters, the Kotlin compiler may still generate capturing lambdas or use internal cast logic that fails when the function is called across classloader boundaries.

The most resilient approach is to **eliminate all parameters except the Paparazzi instance and the content lambda**, and use a **"Side Channel" (System Properties)** to pass mock configuration data.

## Proposed Changes

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Remove all mock-related parameters from `genesysSnapshot` and `genesysResponsiveSnapshot`.
- Inside the `snapshot { ... }` block, read mock configuration from System Properties:
    - `genesys.mock.userId`
    - `genesys.mock.userRole`
    - `genesys.mock.userPermissions`
- Reconstruct the `UserProfile` object **inside** the Koin `single` lambda to ensure zero variable capturing from the outer scope.

#### [MODIFY] [ScreensSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/ScreensSnapshotTest.kt)
- In `testAdminDashboardResponsive`, set System Properties before calling `genesysResponsiveSnapshot` and clear them in a `finally` block.

## Verification Plan

### Automated Tests
- This "Side Channel" approach is immune to classloader-based cast exceptions during function calls because the data is passed via the JVM's global system property map, which is shared and only contains standard `java.lang.String` objects.
- The reconstruction happens entirely within the rendering classloader.
- The user should run `:screenshot-tests:testDebugUnitTest` to verify.
