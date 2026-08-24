# Fix ClassCastException in ScreensSnapshotTest

The test `testAdminDashboardResponsive` is failing with a `java.lang.ClassCastException` at line 66 of `ScreensSnapshotTest.kt`. This line corresponds to the call to `paparazzi.genesysResponsiveSnapshot` passing a `mockUserProfile`.

## Analysis

The `ClassCastException` likely stems from a classloader mismatch in the Paparazzi test environment. Paparazzi uses a custom classloader for rendering (LayoutLib), while the test code runs in the standard JUnit/Android test classloader. When a complex object like `UserProfile` is created in the test and passed into the Paparazzi rendering block, it might be seen as a different class if the rendering environment reloads classes.

Additionally, `GenesysPaparazzi.kt` was creating the Koin `mockModule` outside the `paparazzi.snapshot` block, which further exacerbates classloader issues for the mocked `PageViewModel`.

## Proposed Changes

### [screenshot-tests](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests)

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Move the creation of `mockModule` inside the `this.snapshot` block to ensure it's evaluated within the correct classloader context.
- Ensure all MockK expectations return types that exactly match the expected `StateFlow` types.

#### [MODIFY] [ScreensSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/ScreensSnapshotTest.kt)
- Simplify `UserProfile` creation and ensure proper imports to avoid any naming conflicts.
- Explicitly use `koinInject<PageViewModel>()` to help type inference.

## Verification Plan

### Automated Tests
- Run `:screenshot-tests:testDebugUnitTest --tests "com.itbenevides.genesys21.screenshot.ScreensSnapshotTest.testAdminDashboardResponsive"` to verify the fix.
