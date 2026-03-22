#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TEST_FILE="${REPO_ROOT}/scripts/tests/install_no_jre.bats"

if command -v bats >/dev/null 2>&1; then
  BATS_CMD="bats"
else
  echo "ERROR: bats-core is required to run shell unit tests."
  echo
  case "$(uname)" in
    Darwin)
      echo "Install it on macOS with:"
      echo "  brew install bats-core"
      ;;
    Linux)
      if command -v apt-get >/dev/null 2>&1; then
        echo "Install it on Ubuntu/Debian with:"
        echo "  sudo apt-get install -y bats"
      elif command -v dnf >/dev/null 2>&1; then
        echo "Install it on Fedora with:"
        echo "  sudo dnf install -y bats"
      elif command -v yum >/dev/null 2>&1; then
        echo "Install it on yum-based Linux with:"
        echo "  sudo yum install -y bats"
      else
        echo "Install bats-core using your package manager, then re-run this script."
      fi
      ;;
    *)
      echo "Install bats-core using your package manager, then re-run this script."
      ;;
  esac
  exit 1
fi

exec "${BATS_CMD}" "${TEST_FILE}"
