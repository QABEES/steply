#!/bin/bash
# Simple launcher script (Unix-like)
STEPLY_HOME="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_BIN="$STEPLY_HOME/jre/bin/java"
CLASSPATH="$STEPLY_HOME/lib/*:$STEPLY_HOME/lib/*-jar-with-dependencies.jar"
"$JAVA_BIN" -cp "$CLASSPATH" -Dlogback.configurationFile="$STEPLY_HOME/config/logback.xml" org.jsmart.steply.cli.SteplyCLI "$@"