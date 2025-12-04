#!/system/bin/sh
# Early boot setup
VIRTUALCAM_DIR=/data/adb/virtualcam
mkdir -p "$VIRTUALCAM_DIR/assets"
chmod 770 "$VIRTUALCAM_DIR" "$VIRTUALCAM_DIR/assets"
chown root:root "$VIRTUALCAM_DIR" "$VIRTUALCAM_DIR/assets"

# Allow Zygisk to pick up updated assets/config at boot
if [ -f "$VIRTUALCAM_DIR/refresh.flag" ]; then
  rm -f "$VIRTUALCAM_DIR/refresh.flag"
fi
