# Fix Screenshot Tests Failures

The build failed due to several screenshot tests failing in the `:screenshot-tests` module. Investigation revealed multiple causes including naming mismatches, non-deterministic date rendering, and outdated template IDs.

## User Review Required

> [!IMPORTANT]
> The fixes include changing the naming convention of snapshots in `GenesysPaparazzi.kt` to lowercase (`phone`, `tablet`, `desktop`) to match the existing goldens in the repository. This is critical for Linux CI environments where filenames are case-sensitive.

> [!WARNING]
> I noticed that several templates mentioned in `TemplateShowcaseScreen.kt` (e.g., `pro_design`, `bio_profile`, `blog_post`) are missing from `PageTemplateRegistry.kt`. I will update the code to use the currently available templates to prevent empty screen snapshots, but if these templates were accidentally deleted, they should be restored.

## Proposed Changes

### Core Infrastructure

#### [MODIFY] [GenesysPaparazzi.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/util/GenesysPaparazzi.kt)
- Change configuration names from capitalized ("Phone", "Tablet", "Desktop") to lowercase ("phone", "tablet", "desktop") to match the repository's naming standard and fix CI failures.

### UI Components & Determinism

#### [MODIFY] [CartScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/viewer/CartScreen.kt)
- Update `ModernCartItemRow` to accept an optional `TimeZone` (defaulting to `TimeZone.currentSystemDefault()`) to allow deterministic rendering in tests.

#### [MODIFY] [AdaptiveLayoutsSnapshotTest.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/screenshot-tests/src/test/kotlin/com/itbenevides/genesys21/screenshot/AdaptiveLayoutsSnapshotTest.kt)
- Update `testCartResponsive` to pass `TimeZone.UTC` when rendering `CartContent` to ensure consistent date strings across different CI environments.

### Feature Synchronization

#### [MODIFY] [TemplateShowcaseScreen.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/screens/editor/TemplateShowcaseScreen.kt)
- Update the hardcoded list of templates to use valid IDs from `PageTemplateRegistry` (e.g., `premium_store` instead of `pro_design`, `personal_hub` instead of `bio_profile`).

## Verification Plan

### Automated Tests
- Run the specifically failing tests to verify they now pass:
  ```bash
  ./gradlew :screenshot-tests:testDebugUnitTest --tests com.itbenevides.genesys21.screenshot.AdaptiveLayoutsSnapshotTest
  ./gradlew :screenshot-tests:testDebugUnitTest --tests com.itbenevides.genesys21.screenshot.DesignSystemSnapshotTest
  ./gradlew :screenshot-tests:testDebugUnitTest --tests com.itbenevides.genesys21.screenshot.TemplatesSnapshotTest
  ```
