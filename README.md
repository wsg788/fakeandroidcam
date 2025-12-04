# Fake Android Cam

A sample Android app (minSdk 26) that configures a virtual camera feed. It lets you:

- Pick an image or video to broadcast through the virtual camera and preview it.
- Toggle whether the virtual camera is enabled.
- Select which installed apps can access the virtual camera.
- Detect root/Magisk and request module ZIP installation through Magisk or a root shell.
- Write configuration JSON directly to `/data/adb/virtualcam/config.json` when rooted, or broadcast/binder-fallback when not.

## Modules
- `MediaFragment`: media selection, preview, enablement toggle, config saving.
- `ScopeFragment`: installed-app list with allow toggles.
- `StatusFragment`: root/Magisk detection and module installation helpers.
- `RootHelper` / `ConfigWriter` / `ModuleInstaller`: utilities for privilege escalation, config writes, and Magisk integration.

## Building
Open the project in Android Studio or run `./gradlew assembleDebug` with Android SDK configured.
