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

Example(Success Runs):
```bash
-0.1.0-SNAPSHOT-local.zip
➜  steply git:(mvp_cli) ✗ pwd
/Users/<MYHOMEDIR>/Downloads/STEPLY_WORKSPACE/steply

➜  steply git:(mvp_cli) ✗ 
./scripts/build-distribution-local-jre.sh /Users/<MYHOMEDIR>/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre zip_folder
./scripts/build-distribution-local-jre.sh /Users/nchandra/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre /tmp/steply-dist

```

Running:
./zip_folder/bin/steply.sh --scenario example/github-get-test.json --target example/github.properties --reports ./target/reports
./bin/steply.sh --scenario example/github-get-test.json --target example/github.properties --reports ./target/reports