#!/bin/bash

# The script assumes JARs are in the same directory as the script
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)
SERVER_JAR_PATH="$SCRIPT_DIR/pdffiller.jar"
CLI_JAR_PATH="$SCRIPT_DIR/pdffiller-jar-with-dependencies.jar"

MODE=$1

if [ "$MODE" = "start" ]; then
  PORT=${2:-9001}
  echo "🔥 Starting HTTP server on port $PORT..."
  java -jar "$SERVER_JAR_PATH" --server.port=$PORT
  exit 0
fi

if [ "$MODE" = "fill" ]; then
  echo "🛠️ Running CLI PDF filler..."
  shift
  java -jar "$CLI_JAR_PATH" "$@"
  exit 0
fi

echo "❌ Unknown command: $MODE"
echo "✅ Usage:"
echo "    ./pdffiller.sh start [port]"
echo "    ./pdffiller.sh fill [options]"
