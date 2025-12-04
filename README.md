# Fake Android Virtual Camera (Magisk/Zygisk)

This module bootstraps a virtual camera workspace at `/data/adb/virtualcam`, registers Zygisk
hooks for Camera/Camera2/MediaRecorder, and feeds frames from user-provided media files.
It ships a small native/Java bridge (`zygisk/`) that reads `config.json` to decide which apps
to target and whether to serve images or looping video.

## Features
- Creates a Magisk module scaffold with `module.prop`, `customize.sh`, `post-fs-data.sh`, and
  `service.sh` to prepare `/data/adb/virtualcam` with sane permissions and a default config.
- Zygisk-native bridge (`zygisk/src`) exposes JNI entry points consumed by the Java layer
  (`zygisk/java`) to coordinate per-app routing and asset selection.
- Per-app routing via `/data/adb/virtualcam/config.json` controls whether an app gets video or
  still-frame injection and which asset file to use.
- Safe-by-default defaults: only apps listed in `per_app` get hooked unless you leave the
  wildcard entry enabled.

## Layout
```
module.prop               # Magisk manifest
customize.sh              # Installer seeds /data/adb/virtualcam + default config
post-fs-data.sh           # Early boot: ensures workspace exists
service.sh                # Watches config.json for live reloads
zygisk/include|src        # Native bridge and stub Zygisk lifecycle
zygisk/java               # Java bridge that attaches to Camera/Camera2/MediaRecorder
```

## Installation
1. Build or zip the repository contents into a Magisk module.
2. Flash/install the module in Magisk Manager.
3. Reboot to allow `post-fs-data` to create `/data/adb/virtualcam` and mark the Zygisk service.
4. Place media assets (e.g., `demo.mp4`, `frame.png`) inside `/data/adb/virtualcam/assets/`.
5. Edit `/data/adb/virtualcam/config.json` to target the apps you want. The installer seeds a
   starter config if missing.

## Config format
`/data/adb/virtualcam/config.json` uses a lightweight structure:
```json
{
  "default_asset": "demo.mp4",
  "fallback_image": "frame.png",
  "per_app": {
    "com.example.videochat": {"mode": "video", "asset": "demo.mp4"},
    "com.example.camera":    {"mode": "image", "asset": "frame.png"},
    "*": {"mode": "video", "asset": "demo.mp4"}
  }
}
```
- `mode`: `video` keeps streaming frames; `image` reuses a single frame.
- `asset`: filename inside `/data/adb/virtualcam/assets/`.
- Remove the `"*"` key to disable hooking for apps you do not explicitly list.

The running service watches for changes with `inotifyd` when available and toggles a
`refresh.flag` file so Zygisk can reload without a reboot.

## Hook behavior
- `zygisk/src/main.cpp` registers native methods that drive the Java bridge.
- `zygisk/java/VirtualCamHooks.java` decides when to hook and attaches bridges:
  - `Camera.setPreviewCallback` feeds buffers from the selected asset.
  - Camera2/MediaRecorder bindings are stubbed with logging and placeholder comments where
    a surface pipeline can be wired.
- `zygisk/include/VirtualCamBridge.h` + `zygisk/src/VirtualCamBridge.cpp` parse `config.json`
  and choose the asset/mode for the active package.

## Safety notes
- This module only writes inside `/data/adb/virtualcam` and the Magisk module directory.
- Permissions default to `770/660` to protect assets from other apps; adjust only if you know
  the implications.
- Hooking is limited to packages listed in `config.json`. Keep the list small to reduce the
  attack surface and avoid unexpected camera hijacking.
- Media injection should be tested on secondary devices or emulators before daily use.
