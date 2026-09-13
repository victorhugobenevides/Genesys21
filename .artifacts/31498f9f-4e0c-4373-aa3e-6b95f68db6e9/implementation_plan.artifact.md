# Fix GoogleAuthProvider Initialization Crash

The application is crashing with a `java.lang.IllegalArgumentException: Make sure you invoked GoogleAuthProvider #create method with providing credentials`. This is caused by using `kmpauth` library without initializing the `GoogleAuthProvider` with a `serverClientId`.

## Proposed Changes

### 1. Centralize Auth Constants
Add the `GOOGLE_WEB_CLIENT_ID` to a central location for use across platforms.

#### [MODIFY] [Constants.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/shared/src/commonMain/kotlin/com/itbenevides/genesys21/Constants.kt)
- Add `const val GOOGLE_WEB_CLIENT_ID = "674755208954-6ofmvlcn9birat7ako2banqc9ph1t74s.apps.googleusercontent.com"`

### 2. Platform-Specific Initialization
Update `initializeFirebase()` actual implementations to include `GoogleAuthProvider.create()`.

#### [MODIFY] [FirebaseInitializer.android.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/androidMain/kotlin/com/itbenevides/genesys21/FirebaseInitializer.android.kt)
- Initialize `GoogleAuthProvider` with the `GOOGLE_WEB_CLIENT_ID`.

#### [MODIFY] [FirebaseInitializer.apple.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/appleMain/kotlin/com/itbenevides/genesys21/FirebaseInitializer.apple.kt)
- Initialize `GoogleAuthProvider` with the `GOOGLE_WEB_CLIENT_ID`.

### 3. Trigger Initialization on Android
Ensure `initializeFirebase()` is called during Android app startup.

#### [MODIFY] [MainActivity.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/androidMain/kotlin/com/itbenevides/genesys21/MainActivity.kt)
- Call `initializeFirebase()` in `onCreate()`.

## Verification Plan

### Manual Verification
- Deploy the app to an Android emulator/device.
- Navigate to the login screen.
- Verify that the app no longer crashes when the Google Sign-In button is rendered or clicked.
