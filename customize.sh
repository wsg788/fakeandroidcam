#!/system/bin/sh
# Magisk module installation script

MODPATH=${MODPATH:-/data/adb/modules/fakeandroidcam}
VIRTUALCAM_DIR=/data/adb/virtualcam
CONFIG_FILE="$VIRTUALCAM_DIR/config.json"
ASSETS_DIR="$VIRTUALCAM_DIR/assets"

ui_print "[*] Preparing virtual camera workspace at $VIRTUALCAM_DIR"
mkdir -p "$ASSETS_DIR" || abort "[!] Unable to create $ASSETS_DIR"
chmod 770 "$VIRTUALCAM_DIR" "$ASSETS_DIR"
chown root:root "$VIRTUALCAM_DIR" "$ASSETS_DIR"

if [ ! -f "$CONFIG_FILE" ]; then
  cat <<'CFG' > "$CONFIG_FILE"
{
  "default_asset": "demo.mp4",
  "fallback_image": "frame.png",
  "per_app": {
    "*": {
      "mode": "video",
      "asset": "demo.mp4"
    }
  }
}
CFG
  ui_print "[*] Created default config.json"
fi
chmod 660 "$CONFIG_FILE"

cp -r "$MODPATH"/zygisk "$VIRTUALCAM_DIR" 2>/dev/null || true
chmod -R 750 "$VIRTUALCAM_DIR/zygisk"

ui_print "[*] Virtual camera module files installed"
