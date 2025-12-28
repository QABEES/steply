# Distribution build helper scripts

Two helper scripts are provided:

1. build-distribution.sh
   - Attempts to download a Temurin/Adoptium JRE for macOS/AArch64 (M1/M2/M3).
   - Usage: ./build-distribution.sh [jre-version-tag] [output-dir]
   - Example: ./build-distribution.sh 8u372-b07 /tmp/steply-dist

2. build-distribution-local-jre.sh
   - Uses a JRE you already have locally to assemble the distribution.
   - Usage: ./build-distribution-local-jre.sh /path/to/jre [output-dir]
   - Example: ./build-distribution-local-jre.sh /Library/Java/JavaVirtualMachines/temurin-8.jre/Contents/Home /tmp/steply-dist