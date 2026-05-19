#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

JUNIT_JAR=".tools/junit-platform-console-standalone-1.14.4.jar"
SRC_DIR="src"
TEST_DIR="unittests"

BUILD_DIR="build"
BIN_DIR="$BUILD_DIR/classes"
BIN_TEST_DIR="$BUILD_DIR/test-classes"

usage() {
  cat <<'EOF'
Usage:
  ./run-tests.sh                # compile + run all tests
  ./run-tests.sh --class <FQCN> # run a specific test class, e.g. renderer.CameraTests

Notes:
  - Requires .tools/junit-platform-console-standalone-1.14.4.jar
  - Output folders: build/classes/ and build/test-classes/
EOF
}

SELECT_CLASS=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    --class)
      shift
      SELECT_CLASS="${1:-}"
      if [[ -z "$SELECT_CLASS" ]]; then
        echo "ERROR: --class requires a fully-qualified class name" >&2
        exit 2
      fi
      ;;
    *)
      echo "ERROR: Unknown argument: $1" >&2
      usage
      exit 2
      ;;
  esac
  shift
done

if [[ ! -f "$JUNIT_JAR" ]]; then
  echo "ERROR: Missing $JUNIT_JAR" >&2
  echo "If you ignored it in git, keep it locally under .tools/" >&2
  exit 2
fi

command -v java >/dev/null 2>&1 || { echo "ERROR: java not found in PATH" >&2; exit 2; }
command -v javac >/dev/null 2>&1 || { echo "ERROR: javac not found in PATH" >&2; exit 2; }

echo "==> Cleaning stray .class under $SRC_DIR (and $TEST_DIR)"
find "$SRC_DIR" -name '*.class' -delete 2>/dev/null || true
find "$TEST_DIR" -name '*.class' -delete 2>/dev/null || true

rm -rf "$BIN_DIR" "$BIN_TEST_DIR"
mkdir -p "$BIN_DIR" "$BIN_TEST_DIR"

echo "==> Compiling sources ($SRC_DIR -> $BIN_DIR)"
find "$SRC_DIR" -name '*.java' -print0 | xargs -0 javac -d "$BIN_DIR"

echo "==> Compiling tests ($TEST_DIR -> $BIN_TEST_DIR)"
find "$TEST_DIR" -name '*.java' -print0 | xargs -0 javac \
  -cp "$BIN_DIR:$JUNIT_JAR" \
  -d "$BIN_TEST_DIR"

echo "==> Running tests"
if [[ -n "$SELECT_CLASS" ]]; then
  java -jar "$JUNIT_JAR" \
    --class-path "$BIN_DIR:$BIN_TEST_DIR" \
    --select-class "$SELECT_CLASS"
else
  java -jar "$JUNIT_JAR" \
    --class-path "$BIN_DIR:$BIN_TEST_DIR" \
    --scan-class-path
fi
