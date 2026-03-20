#!/usr/bin/env bash
# Installs Steply without a bundled JRE.
# Requires Java 17+ to be available on the PATH.
# Intended for CI environments (e.g. GitHub Actions or GitLab Pipeline).
#
# Usage:
#   curl -fsSL https://raw.githubusercontent.com/QABEES/steply/main/scripts/install_no_jre.sh | bash

set -euo pipefail

VERSION="20260320.02" #This is the only variable to update when releasing a new version.
ZIP_NAME="steply-${VERSION}-no-jre.zip"
ZIP_URL="https://github.com/QABEES/steply/releases/download/${VERSION}/${ZIP_NAME}"

INSTALL_ROOT="${XDG_DATA_HOME:-$HOME/.local/share}/steply"
INSTALL_DIR="${INSTALL_ROOT}/${VERSION}"
BIN_DIR="$HOME/.local/bin"
LAUNCHER="${BIN_DIR}/steply"

# Path to /etc/os-release — overridable in tests
OS_RELEASE_FILE="${OS_RELEASE_FILE:-/etc/os-release}"

# ---------------------------------------------------------------------------
# Testable helpers — override these in unit tests to avoid real system calls
# ---------------------------------------------------------------------------

_has_command()         { command -v "$1" &>/dev/null; }
_get_uname()           { uname; }
_java_version_output() { java -version 2>&1; }

# ---------------------------------------------------------------------------
# OS Detection
# ---------------------------------------------------------------------------

detect_os() {
  if _has_command apt-get; then
    echo "debian"
  elif _has_command dnf; then
    echo "fedora"
  elif _has_command yum; then
    if grep -qi "Amazon Linux 2" "${OS_RELEASE_FILE}" 2>/dev/null; then
      echo "amazon-linux"
    else
      echo "fedora-yum"
    fi
  elif [[ "$(_get_uname)" == "Darwin" ]]; then
    if _has_command brew; then
      echo "macos-brew"
    else
      echo "macos-no-brew"
    fi
  else
    echo "unknown"
  fi
}

# ---------------------------------------------------------------------------
# Java
# ---------------------------------------------------------------------------

# On macOS without Java, /usr/bin/java is a stub that passes `command -v java`
# but fails when actually invoked, so we must test execution, not just presence.
get_java_major_version() {
  local output
  output="$(_java_version_output)" || { echo "0"; return; }
  echo "${output}" | grep -oE '"[0-9]+' | head -1 | tr -d '"' || echo "0"
}

install_java_debian()     { sudo apt-get install -y openjdk-17-jre-headless; }
install_java_fedora()     { sudo dnf install -y java-17-openjdk-headless; }
install_java_fedora_yum() { sudo yum install -y java-17-openjdk-headless; }
install_java_amazon() {
  sudo amazon-linux-extras enable corretto17 2>/dev/null || true
  sudo yum install -y java-17-amazon-corretto-headless
}
install_java_brew() { brew install openjdk@17; }
install_java_macos_no_brew() {
  echo "ERROR: Java 17 not found and Homebrew is not installed."
  echo "Please install Java 17 (or higher) manually and re-run this script."
  echo ""
  echo "Option 1) Recommended: Install Homebrew first (https://brew.sh), then re-run this script."
  echo ""
  echo "Option 2) Download Java Temurin 17 (free, open source) from below and re-run the script:"
  echo "   https://adoptium.net/temurin/releases/?version=17&os=mac&package=jre"
  exit 1
}

ensure_java() {
  local java_major
  java_major="$(get_java_major_version)"
  if [[ "${java_major}" -ge 17 ]]; then
    echo "Found Java 17+ (version ${java_major}). Performing next steps..."
    return 0
  fi
  echo "Java 17+ not found — attempting to install Java 17..."
  local os
  os="$(detect_os)"
  case "${os}" in
    debian)        install_java_debian ;;
    fedora)        install_java_fedora ;;
    amazon-linux)  install_java_amazon ;;
    fedora-yum)    install_java_fedora_yum ;;
    macos-brew)    install_java_brew ;;
    macos-no-brew) install_java_macos_no_brew ;;
    *)
      echo "ERROR: Could not install Java 17 automatically. Please install Java 17+ manually and re-run."
      exit 1
      ;;
  esac
}

# ---------------------------------------------------------------------------
# unzip
# ---------------------------------------------------------------------------

install_unzip_debian() { sudo apt-get install -y unzip; }
install_unzip_fedora() { sudo dnf install -y unzip; }
install_unzip_yum()    { sudo yum install -y unzip; }
install_unzip_brew()   { brew install unzip; }

ensure_unzip() {
  if _has_command unzip; then
    return 0
  fi
  echo "'unzip' not found — attempting to install..."
  local os
  os="$(detect_os)"
  case "${os}" in
    debian)                  install_unzip_debian ;;
    fedora)                  install_unzip_fedora ;;
    amazon-linux|fedora-yum) install_unzip_yum ;;
    macos-brew)              install_unzip_brew ;;
    *)
      echo "ERROR: Could not install 'unzip' automatically. Please install it manually and re-run."
      exit 1
      ;;
  esac
}

# ---------------------------------------------------------------------------
# Download & Install
# ---------------------------------------------------------------------------

download_zip() {
  echo "Downloading: ${ZIP_URL}"
  curl -fsSL "${ZIP_URL}" -o "${TMP_DIR}/${ZIP_NAME}"
}

install_steply() {
  echo "Installing to: ${INSTALL_DIR}"
  rm -rf "${INSTALL_DIR:?}/"*
  unzip -q "${TMP_DIR}/${ZIP_NAME}" -d "${INSTALL_DIR}"

  local steply_sh
  steply_sh="$(find "${INSTALL_DIR}" -type f -path '*/bin/steply.sh' -print -quit || true)"

  if [[ -z "${steply_sh}" ]]; then
    echo "ERROR: Could not find bin/steply.sh after unzip."
    echo "Please check the zip layout: ${ZIP_URL}"
    echo
    echo "DEBUG: Top-level entries in ${INSTALL_DIR}:"
    ls -la "${INSTALL_DIR}" || true
    exit 1
  fi

  DIST_DIR="$(cd "$(dirname "${steply_sh}")/.." && pwd)"
  chmod +x "${steply_sh}"

  cat > "${LAUNCHER}" <<LAUNCHER_EOF
#!/usr/bin/env bash
exec "${steply_sh}" "\$@"
LAUNCHER_EOF
  chmod +x "${LAUNCHER}"

  echo
  echo "Installed Steply (no-JRE) ${VERSION}."
  echo "Install dir: ${DIST_DIR}"
  echo "Binary: ${LAUNCHER}"
  echo
}

# ---------------------------------------------------------------------------
# PATH setup
# ---------------------------------------------------------------------------

setup_path() {
  if echo ":${PATH}:" | grep -q ":${BIN_DIR}:"; then
    return 0
  fi
  local export_line="export PATH=\"${BIN_DIR}:\$PATH\""
  local profile
  for profile in "${HOME}/.bashrc" "${HOME}/.zshrc"; do
    if [[ -f "${profile}" ]] && ! grep -qF "${BIN_DIR}" "${profile}"; then
      echo "" >> "${profile}"
      echo "# Added by Steply installer" >> "${profile}"
      echo "${export_line}" >> "${profile}"
      echo "Added ${BIN_DIR} to PATH in ${profile}."
    fi
  done
  echo
  echo "NOTE: To use 'steply' in this session, run:"
  echo "  ${export_line}"
  echo
}

# ---------------------------------------------------------------------------
# Verify
# ---------------------------------------------------------------------------

verify_installation() {
  if "${LAUNCHER}" -v; then
    echo "Steply installed successfully."
  else
    echo "ERROR: Steply installation verification failed. Please check the install."
    exit 1
  fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

main() {
  ensure_java
  ensure_unzip

  mkdir -p "${INSTALL_DIR}" "${BIN_DIR}"

  TMP_DIR="$(mktemp -d)"
  cleanup() { rm -rf "${TMP_DIR}"; }
  trap cleanup EXIT

  download_zip
  install_steply
  setup_path
  verify_installation
}

# FIXED: run main when executed directly or piped via curl|bash.
# Skip main when sourced by tests (BASH_SOURCE[0] is set but differs from $0).
#   curl|bash → BASH_SOURCE[0] is unset/empty → run main
#   ./script  → BASH_SOURCE[0] == $0           → run main
#   source    → BASH_SOURCE[0] != $0           → skip main
if [[ -z "${BASH_SOURCE[0]:-}" ]] || [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  main "$@"
fi
