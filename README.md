```
BUILD:
======
mvn clean  install -DskipTests
./scripts/build-distribution-local-jre.sh /Users/nchandra/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre /tmp/steply-dist
cp steply-cli/target/*-jar-with-dependencies.jar /private/tmp/steply-dist/lib/
./scripts/build-distribution-local-jre.sh /Users/nchandra/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre /tmp/steply-dist

RUN:
=====
➜  random pwd
/Users/nchandra/Downloads/STEPLY_WORKSPACE/random
steply --scenario example/hello_world_status_ok_assertions_new.json --target example/github_host_new.properties                        

or:

➜  cd steply-dist
./bin/steply.sh --scenario example/hello_world_status_ok_assertions_new.json --target example/github_host_new.properties

```

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
  - `build-distribution-local-jre.sh` — assemble distribution using a local JRE path you provide. (SEE BELOW HOW TO RUN)

See `scripts/README-distribution.md` for usage examples.

**************************************************
=> PREPARE THE CONTENT FOR ZIP FILE:
```shell
Quick one-off fix (if you want to test now without changing scripts)

Build the CLI jar: 
mvn -pl steply-cli -am package -DskipTests

Copy the jar into your distribution's lib manually: 
cp steply-cli/target/*-jar-with-dependencies.jar /private/tmp/steply-dist/lib/
```

=> BUILD THE ZIP FILE:
MAC:
```shell
./scripts/build-distribution-local-jre.sh /Users/nchandra/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre /tmp/steply-dist
```

RUN:
```
steply --scenario example/hello_world_status_ok_assertions_new.json --target example/github_host_new.properties --reports ./target/reports
```

RUN THE TEST:
```shell
➜  steply-dist pwd
/private/tmp/steply-dist

Make sure bin/steply.sh is executable, then run:
cd /private/tmp/steply-dist

➜  steply-dist 
./bin/steply.sh --scenario example/github-get-test.json --target example/github.properties --reports ./target/reports

========================================
Steply Test Execution v0.1.0-SNAPSHOT
========================================
Scenario: example/github-get-test.json
Target: example/github.properties
Report: ./target/reports
========================================
Executing tests...

Total: 1
Passed: 1
Failed: 0
Duration: 0ms
========================================
Reports generated at: ./target/reports/steply-report
========================================
➜  steply-dist 
```

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ RUNNING @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

FROM PROJECT ROOT:
=> Using --scenario:
java -jar steply-cli/target/steply-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar
--scenario example/github-get-test.json
--target example/github.properties
--reports ./target/reports
--log-level INFO

=> Using -s :
java -jar steply-cli/target/steply-cli-0.1.0-SNAPSHOT-jar-with-dependencies.jar -s example/github-get-test.json -t example/github.properties -r ./target/reports -l INFO

=> Build Distribution zip:
./scripts/build-distribution-local-jre.sh /Users/<MYHOMEDIR>/.sdkman/candidates/java/current/zulu-8.jdk/Contents/Home/jre zip_folder


# STEPLY CLI
export PATH="/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply-dist/bin:$PATH
(Not necessary if you're creating a Symlink to bin/steply.sh in your /usr/local/bin or similar)

DO THIS FOR SYMLINK:
```shell
sudo tee /usr/local/bin/steply > /dev/null <<'EOF'
#!/bin/bash
exec "/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply-dist/bin/steply.sh" "$@"
EOF
sudo chmod +x /usr/local/bin/steply
```

REVIEW THE SYMLINK:
```
➜  ~ view /usr/local/bin/steply                    
#!/bin/bash
exec "/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply-dist/bin/steply.sh" "$@"
```

TEST THE SYMLINK:
Run it from anywhere, where you have the "example" folder available:
```
➜  ~ steply -h                                         
Error parsing arguments: Missing required option: t
usage: steply
 -f,--folder <arg>      Folder containing multiple scenarios
 -h,--help              Show help
 -l,--log-level <arg>   Logging level (WARN/INFO/DEBUG)
 -r,--reports <arg>     Custom report output directory (default: ./target)
 -s,--scenario <arg>    Single scenario file path
 -t,--target <arg>      Target environment properties file
 -v,--version           Show version information

Running example:
----------------
➜  steply-dist pwd
/Users/nchandra/Downloads/STEPLY_WORKSPACE/steply-dist
➜  steply-dist 

➜  steply-dist steply --scenario example/github-get-test.json --target example/github.properties --reports ./target/reports
========================================
Steply Test Execution v0.1.0-SNAPSHOT
========================================
Scenario: example/github-get-test.json
Target: example/github.properties
Report: ./target/reports
========================================
Executing tests...

Total: 1
Passed: 1
Failed: 0
Duration: 0ms
========================================
Reports generated at: ./target/reports/steply-report
========================================
```

RUN FROM RANDOM FOLDER(TESTED):
```shell
➜  random_folder pwd
/Users/nchandra/Downloads/STEPLY_WORKSPACE/random_folder

➜  random_folder ls -l  
total 0
drwxr-xr-x  4 nchandra  staff  128 29 Dec 01:14 example

➜  random_folder steply --scenario example/github-get-test.json --target example/github.properties --reports ./target/reports
========================================
Steply Test Execution v0.1.0-SNAPSHOT
========================================
Scenario: example/github-get-test.json
Target: example/github.properties
Report: ./target/reports
========================================
Executing tests...

Total: 1
Passed: 1
Failed: 0
Duration: 0ms
========================================
Reports generated at: ./target/reports/steply-report
========================================
```
