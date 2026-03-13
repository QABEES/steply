#!/usr/bin/env bash
# Installs Steply without a bundled JRE.
# Requires Java 17+ to be available on the PATH.
# Intended for CI environments (e.g. GitHub Actions or GitLab Pipeline).
#
# Usage:
#   curl -fsSL https://raw.githubusercontent.com/QABEES/steply/main/scripts/install_no_jre.sh | bash

set -euo pipefail

RELEASE_TAG="20260314.01" #This is the release tag name while publishing to GitHub "releases" section
ZIP_NAME="steply-20260314.01-no-jre.zip" #This is the exact zip file name in the GitHub "releases" section.

ZIP_URL="https://github.com/QABEES/steply/releases/download/${RELEASE_TAG}/${ZIP_NAME}"

INSTALL_ROOT="${XDG_DATA_HOME:-$HOME/.local/share}/steply"
INSTALL_DIR="${INSTALL_ROOT}/${RELEASE_TAG}"
BIN_DIR="$HOME/.local/bin"
LAUNCHER="${BIN_DIR}/steply"

# Verify Java is available, install Java 17 if not found
if ! command -v java &>/dev/null; then
  echo "Java not found — attempting to install Java 17..."
  if command -v apt-get &>/dev/null; then
    sudo apt-get install -y openjdk-17-jre-headless
  elif command -v dnf &>/dev/null; then
    sudo dnf install -y java-17-openjdk-headless
  elif command -v yum &>/dev/null; then
    if grep -qi "Amazon Linux 2" /etc/os-release 2>/dev/null; then
      sudo amazon-linux-extras enable corretto17 2>/dev/null || true
      sudo yum install -y java-17-amazon-corretto-headless
    else
      sudo yum install -y java-17-openjdk-headless
    fi
  elif command -v brew &>/dev/null; then
    brew install openjdk@17
  else
    echo "ERROR: Could not install Java 17 automatically. Please install Java 17+ manually and re-run."
    exit 1
  fi
fi

# Verify unzip is available
if ! command -v unzip &>/dev/null; then
  echo "'unzip' not found — attempting to install..."
  if command -v apt-get &>/dev/null; then
    sudo apt-get install -y unzip
  elif command -v dnf &>/dev/null; then
    sudo dnf install -y unzip
  elif command -v yum &>/dev/null; then
    sudo yum install -y unzip
  elif command -v brew &>/dev/null; then
    brew install unzip
  else
    echo "ERROR: Could not install 'unzip' automatically. Please install it manually and re-run."
    exit 1
  fi
fi

mkdir -p "${INSTALL_DIR}" "${BIN_DIR}"

TMP_DIR="$(mktemp -d)"
cleanup() { rm -rf "${TMP_DIR}"; }
trap cleanup EXIT

echo "Downloading: ${ZIP_URL}"
curl -fsSL "${ZIP_URL}" -o "${TMP_DIR}/${ZIP_NAME}"

echo "Installing to: ${INSTALL_DIR}"
rm -rf "${INSTALL_DIR:?}/"*
unzip -q "${TMP_DIR}/${ZIP_NAME}" -d "${INSTALL_DIR}"

# Find steply.sh regardless of whether the zip has a top-level directory or not.
STEPLY_SH="$(find "${INSTALL_DIR}" -type f -path '*/bin/steply.sh' -print -quit || true)"

if [[ -z "${STEPLY_SH}" ]]; then
  echo "ERROR: Could not find bin/steply.sh after unzip."
  echo "Please check the zip layout: ${ZIP_URL}"
  echo
  echo "DEBUG: Top-level entries in ${INSTALL_DIR}:"
  ls -la "${INSTALL_DIR}" || true
  exit 1
fi

DIST_DIR="$(cd "$(dirname "${STEPLY_SH}")/.." && pwd)"

chmod +x "${STEPLY_SH}"

# Create a user-local launcher on PATH
cat > "${LAUNCHER}" <<EOF
#!/usr/bin/env bash
exec "${STEPLY_SH}" "\$@"
EOF
chmod +x "${LAUNCHER}"

echo
echo "Installed Steply (no-JRE) ${RELEASE_TAG}."
echo "Install dir: ${DIST_DIR}"
echo "Binary: ${LAUNCHER}"
echo

if ! echo ":$PATH:" | grep -q ":${BIN_DIR}:"; then
  EXPORT_LINE="export PATH=\"${BIN_DIR}:\$PATH\""
  for PROFILE in "$HOME/.bashrc" "$HOME/.zshrc"; do
    if [[ -f "$PROFILE" ]] && ! grep -qF "${BIN_DIR}" "$PROFILE"; then
      echo "" >> "$PROFILE"
      echo "# Added by Steply installer" >> "$PROFILE"
      echo "${EXPORT_LINE}" >> "$PROFILE"
      echo "Added ${BIN_DIR} to PATH in ${PROFILE}."
    fi
  done
  echo
  echo "NOTE: To use 'steply' in this session, run:"
  echo "  ${EXPORT_LINE}"
  echo
fi

if "${LAUNCHER}" -v; then
  echo "Steply installed successfully."
else
  echo "ERROR: Steply installation verification failed. Please check the install."
  exit 1
fi