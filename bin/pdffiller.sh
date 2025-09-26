#!/bin/bash

MODE=$1

# Đường dẫn đến file JAR
JAR_PATH="$(dirname "$0")/../target/pdffiller.jar"

if [ "$MODE" = "start" ]; then
  PORT=$2
  if [ -z "$PORT" ]; then PORT=8080; fi
  echo "🔥 Starting HTTP server on port $PORT..."
  java -jar "$JAR_PATH" --server.port=$PORT
  exit 0
fi

if [ "$MODE" = "fill" ]; then
  TEMPLATE=$2
  OUTPUT=$3
  IMAGE=$4
  if [ -z "$TEMPLATE" ] || [ -z "$OUTPUT" ] || [ -z "$IMAGE" ]; then
    echo "❌ Missing parameters."
    echo "✅ Usage: ./pdffiller.sh fill template.pdf output.pdf avatar.png"
    exit 1
  fi
  echo "🛠️ Running CLI PDF filler..."
  java -cp "$JAR_PATH" vn.quyen.cli.PdfFillerCli "$TEMPLATE" "$OUTPUT" "$IMAGE"
  exit 0
fi

echo "❌ Unknown command: $MODE"
echo "✅ Usage:"
echo "    ./pdffiller.sh start [port]"
echo "    ./pdffiller.sh fill template.pdf output.pdf avatar.png"