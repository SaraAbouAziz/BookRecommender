#!/usr/bin/env bash
# Avvia serverBR-1.0.jar in background
set -euo pipefail

# directory dello script (progetto root)
DIR="$(cd "$(dirname "$0")" && pwd)"

# percorso alla cartella bin e ai log
BIN_DIR="$DIR/bin"
LOG_DIR="$DIR/logs"
mkdir -p "$LOG_DIR"

cd "$BIN_DIR"

LOG_FILE="$LOG_DIR/server.log"
PID_FILE="$DIR/server.pid"

nohup java -jar serverBR-1.0.jar > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
echo "serverBR avviato (PID $(cat "$PID_FILE")), log: $LOG_FILE"