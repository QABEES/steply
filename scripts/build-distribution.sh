#!/usr/bin/env bash
# Build distribution automatically for macos-aarch64 by downloading a Temurin (Adoptium) JRE.
# Usage: ./build-distribution.sh [version] [output-dir]
# Example: ./build-distribution.sh 8u372-b07 /tmp/steply-dist

set -euo pipefail

ZEROCODE_VERSION=${ZEROCODE_VERSION:-1.6.0}
STEP_VERSION=${STEP_VERSION:-0.1.0-SNAPSHOT}
JRE_VERSION=${1:-8u372-b07}
OUTDIR=${2:-./dist}

WORKDIR=$(pwd)
BUILD_DIR="$WORKDIR/target/assembly"
mkdir -p "$BUILD_DIR"

echo "Building distribution into $OUTDIR"
rm -rf "$OUTDIR"
mkdir -p "$OUTDIR/bin" "$OUTDIR/lib" "$OUTDIR/config" "$OUTDIR/example"

# copy built jars (user must run mvn package first)
echo "Copying CLI jar (jar-with-dependencies) to lib/"
cp -v target/steply-cli/target/*-jar-with-dependencies.jar "$OUTDIR/lib/" || true

# copy config and example
cp -r config/* "$OUTDIR/config/" || true
cp -r example/* "$OUTDIR/example/" || true

# download Temurin JRE for macos-aarch64 (Temurin archive URL may change — adjust accordingly)
# This is approximate — if the automatic download fails, use the local JRE script.
JRE_TAR="OpenJDK8U-jre_aarch64_mac_hotspot_${JRE_VERSION}.tar.gz"
JRE_URL="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u-${JRE_VERSION}/${JRE_TAR}"
echo "Attempting to download JRE from ${JRE_URL}"
curl -fSL -o "/tmp/${JRE_TAR}" "${JRE_URL}" || { echo "Failed to download JRE. Use build-distribution-local-jre.sh with a local JRE."; exit 1; }
mkdir -p "$OUTDIR/jre"
tar -xzf "/tmp/${JRE_TAR}" -C /tmp
# Many Temurin tarballs unpack to a folder — move bin & lib
JRE_DIR=$(tar -tf "/tmp/${JRE_TAR}" | head -1 | sed -e 's@/.*@@')
mv "/tmp/${JRE_DIR}/"/* "$OUTDIR/jre/"

# copy scripts
cp -r scripts/* "$OUTDIR/bin/"
chmod +x "$OUTDIR/bin/"*.sh || true

# copy top-level files
cp LICENSE README.md VERSION.txt "$OUTDIR/"

# zip
ZIPNAME="steply-${STEP_VERSION}-macos-aarch64.zip"
cd "$OUTDIR/.."
zip -r "$ZIPNAME" "$(basename "$OUTDIR")"
shasum -a 256 "$ZIPNAME" > "$ZIPNAME".sha256
echo "Distribution created: $(pwd)/$ZIPNAME"