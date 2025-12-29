#!/usr/bin/env bash
# Build distribution using a local JRE path.
# Usage: ./build-distribution-local-jre.sh /path/to/jre [output-dir]
# Example: ./build-distribution-local-jre.sh /Library/Java/JavaVirtualMachines/temurin-8.jre/Contents/Home /tmp/steply-dist

set -euo pipefail

LOCAL_JRE=${1:-}
OUTDIR=${2:-./dist}

if [ -z "$LOCAL_JRE" ]; then
  echo "Usage: $0 /path/to/jre [output-dir]"
  exit 1
fi

if [ ! -d "$LOCAL_JRE" ]; then
  echo "Provided JRE path does not exist: $LOCAL_JRE"
  exit 1
fi

WORKDIR=$(pwd)
rm -rf "$OUTDIR"
mkdir -p "$OUTDIR/jre" "$OUTDIR/lib" "$OUTDIR/bin" "$OUTDIR/config" "$OUTDIR/example"

# Ensure CLI jar exists (built as jar-with-dependencies)
CLI_JAR=$(ls steply-cli/target/*-jar-with-dependencies.jar 2>/dev/null || true)
if [ -z "$CLI_JAR" ]; then
  echo "Error: CLI jar-with-dependencies not found. Build it first with:"
  echo "  mvn -pl steply-cli -am package -DskipTests"
  exit 1
fi

# copy jre contents
cp -r "$LOCAL_JRE/"* "$OUTDIR/jre/"

# copy lib (built jars)
echo "Copying CLI jar to lib/"
cp -v "$CLI_JAR" "$OUTDIR/lib/"

cp -r config/* "$OUTDIR/config/" || true
cp -r example/* "$OUTDIR/example/" || true
cp -r scripts/* "$OUTDIR/bin/"
chmod +x "$OUTDIR/bin/"*.sh || true

cp LICENSE README.md VERSION.txt "$OUTDIR/" || true

ZIPNAME="steply-$(cat VERSION.txt | head -n1 | sed 's/steply.version=//')-local.zip"
cd "$OUTDIR/.."
zip -r "$ZIPNAME" "$(basename "$OUTDIR")"
shasum -a 256 "$ZIPNAME" > "$ZIPNAME".sha256
echo "Distribution created: $(pwd)/$ZIPNAME"