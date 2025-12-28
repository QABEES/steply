# Steply — MVP

This repository contains an MVP implementation for the "Single Scenario CLI" described in issue #2.

Modules:
- steply-core: runner, config loader, report generator (MVP)
- steply-cli: CLI entrypoint (commons-cli)
- steply-distribution: assembly descriptor + scripts to build distribution zip

How to build locally:
1. Update zerocode version (optional) in the parent `pom.xml` property `zerocode.version`.
2. Run: `mvn -T1C clean package -DskipTests`

Distribution:
- There are two scripts in `scripts/`:
  - `build-distribution.sh` — download a Temurin macOS/AArch64 JRE and assemble distribution (automatic).
  - `build-distribution-local-jre.sh` — assemble distribution using a local JRE path you provide.

See `scripts/README-distribution.md` for usage examples.

License: Apache-2.0

@@@@@@@@@@@@@@@@@@@@

FROM PROJECT ROOT:
=> Using --scenario:
java -jar steply-cli/target/steply-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar
--scenario example/github-get-test.json
--target example/github.properties
--reports ./target/reports
--log-level INFO

=> Using -s :
java -jar steply-cli/target/steply-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar -s example/github-get-test.json -t example/github.properties -r ./target/reports -l INFO
