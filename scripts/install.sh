#!/usr/bin/env bash
set -euo pipefail

VERSION="20260314.02" #This is the only variable to update when releasing a new version.
ZIP_NAME="steply-${VERSION}.zip"

ZIP_URL="https://github.com/QABEES/steply/releases/download/${VERSION}/${ZIP_NAME}"

INSTALL_ROOT="${XDG_DATA_HOME:-$HOME/.local/share}/steply"
INSTALL_DIR="${INSTALL_ROOT}/${VERSION}"
BIN_DIR="$HOME/.local/bin"
LAUNCHER="${BIN_DIR}/steply"

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

# The zip expands into a top-level steply-dist/ directory.
DIST_DIR="${INSTALL_DIR}/steply-dist"
STEPLY_SH="${DIST_DIR}/bin/steply.sh"

if [[ ! -f "${STEPLY_SH}" ]]; then
  echo "ERROR: Expected ${STEPLY_SH} to exist after unzip, but it was not found."
  echo "Please check the zip layout: ${ZIP_URL}"
  exit 1
fi

chmod +x "${STEPLY_SH}"

# Create a user-local launcher on PATH
cat > "${LAUNCHER}" <<EOF
#!/usr/bin/env bash
exec "${STEPLY_SH}" "\$@"
EOF
chmod +x "${LAUNCHER}"

echo
echo "Installed Steply ${VERSION}."
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
echo ""
echo "👉 Run the following command to test Steply:"
echo "steply --scenario tests/get_user_api.json --target-env env/sit1.properties"
echo ""
