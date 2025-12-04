#!/system/bin/sh
# Starts background watcher to monitor config changes
VIRTUALCAM_DIR=/data/adb/virtualcam
CONFIG_FILE="$VIRTUALCAM_DIR/config.json"
LOG_FILE="$VIRTUALCAM_DIR/service.log"

mkdir -p "$VIRTUALCAM_DIR"
chmod 770 "$VIRTUALCAM_DIR"

log() {
  echo "[service $(date +%Y-%m-%dT%H:%M:%S%z)] $1" >> "$LOG_FILE"
}

watch_config() {
  local last_hash=""
  while true; do
    if [ -f "$CONFIG_FILE" ]; then
      local new_hash
      new_hash=$(sha1sum "$CONFIG_FILE" | awk '{print $1}')
      if [ "$new_hash" != "$last_hash" ]; then
        log "config.json changed; requesting Zygisk refresh"
        touch "$VIRTUALCAM_DIR/refresh.flag"
        last_hash="$new_hash"
      fi
    fi
    sleep 10
  done
}

watch_config &
