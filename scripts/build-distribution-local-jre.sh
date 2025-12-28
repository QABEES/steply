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

# copy jre contents
cp -r "$LOCAL_JRE/"* "$OUTDIR/jre/"

# copy lib (built jars)
cp -v target/steply-cli/target/*-jar-with-dependencies.jar "$OUTDIR/lib/" || true

cp -r config/* "$OUTDIR/config/" || true
cp -r example/* "$OUTDIR/example/" || true
cp -r scripts/* "$OUTDIR/bin/"
chmod +x "$OUTDIR/bin/"*.sh || true

cp LICENSE README.md VERSION.txt "$OUTDIR/"

ZIPNAME="steply-$(cat VERSION.txt | head -n1 | sed 's/steply.version=//')-local.zip"
cd "$OUTDIR/.."
zip -r "$ZIPNAME" "$(basename "$OUTDIR")"
shasum -a 256 "$ZIPNAME" > "$ZIPNAME".sha256
echo "Distribution created: $(pwd)/$ZIPNAME"