#!/usr/bin/env bash
set -euo pipefail

VERSION="0.1.0-20260109"
ZIP_NAME="steply-${VERSION}.zip"
ZIP_URL="https://github.com/QABEES/steply/releases/download/${VERSION}/${ZIP_NAME}"

INSTALL_ROOT="${XDG_DATA_HOME:-$HOME/.local/share}/steply"
INSTALL_DIR="${INSTALL_ROOT}/${VERSION}"
BIN_DIR="$HOME/.local/bin"
LAUNCHER="${BIN_DIR}/steply"

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
echo "Launcher:    ${LAUNCHER}"
echo

if ! echo ":$PATH:" | grep -q ":${BIN_DIR}:"; then
  echo "NOTE: ${BIN_DIR} is not on your PATH."
  echo "Add this line to your shell profile (~/.bashrc, ~/.zshrc, etc):"
  echo "  export PATH=\"${BIN_DIR}:\$PATH\""
  echo
fi

# Refresh the shell's command-lookup cache so command -v picks up the new launcher.
hash -r 2>/dev/null || true

RESOLVED_CMD="$(command -v steply 2>/dev/null || true)"
if [[ -z "${RESOLVED_CMD}" ]]; then
  echo "Resolved path: (not found - '${BIN_DIR}' may not be on PATH yet)"
  echo "Open a new terminal or run:  export PATH=\"${BIN_DIR}:\$PATH\""
elif [[ "${RESOLVED_CMD}" != "${LAUNCHER}" ]]; then
  echo "Resolved path: ${RESOLVED_CMD}"
  echo
  echo "WARNING: 'steply' resolves to a different location than the new launcher."
  echo "         Expected: ${LAUNCHER}"
  echo "         Got:      ${RESOLVED_CMD}"
  echo
  echo "A legacy wrapper is shadowing the new install.  To remove it, run:"
  echo "  sudo rm -f \"${RESOLVED_CMD}\""
  echo "Then refresh your shell cache:"
  echo "  hash -r 2>/dev/null || true"
else
  echo "Resolved path: ${RESOLVED_CMD}  (OK)"
fi

echo
echo "Try:"
echo "  steply -v"
