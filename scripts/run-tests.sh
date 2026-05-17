#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

JUNIT_VER="1.14.4"
TOOLS_DIR="$ROOT_DIR/.tools"
JUNIT_JAR="$TOOLS_DIR/junit-platform-console-standalone-$JUNIT_VER.jar"

BUILD_DIR="$ROOT_DIR/.build/test"
CLASSES_DIR="$BUILD_DIR/classes"

mkdir -p "$TOOLS_DIR" "$CLASSES_DIR"

if [[ ! -f "$JUNIT_JAR" ]]; then
  echo "Downloading JUnit Console Launcher $JUNIT_VER..."
  curl -fsSL -o "$JUNIT_JAR" \
    "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$JUNIT_VER/junit-platform-console-standalone-$JUNIT_VER.jar"
fi

# Compile production sources
find src -name '*.java' > "$BUILD_DIR/sources.txt"
javac -d "$CLASSES_DIR" @"$BUILD_DIR/sources.txt"

# Compile tests
find unittests -name '*.java' > "$BUILD_DIR/tests.txt"
javac -cp "$CLASSES_DIR:$JUNIT_JAR" -d "$CLASSES_DIR" @"$BUILD_DIR/tests.txt"

# Run all tests
java -jar "$JUNIT_JAR" \
  --class-path "$CLASSES_DIR" \
  --scan-class-path
