#!/usr/bin/env bash
# Avvia clientBR-1.0.jar in background
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
BIN_DIR="$DIR/bin"
LOG_DIR="$DIR/logs"
mkdir -p "$LOG_DIR"

cd "$BIN_DIR"

LOG_FILE="$LOG_DIR/client.log"
PID_FILE="$DIR/client.pid"

nohup java -jar clientBR-1.0.jar -cl > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
echo "clientBR avviato (PID $(cat "$PID_FILE")), log: $LOG_FILE"