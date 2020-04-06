#!/bin/sh

set -e

GUIDES_MAVEN_SYSTEM_PROPERTIES="${GUIDES_MAVEN_SYSTEM_PROPERTIES:--Xms256m -Xmx256m}"
GUIDES_GRADLE_SYSTEM_PROPERTIES="${GUIDES_GRADLE_SYSTEM_PROPERTIES:--Dorg.gradle.jvmargs="-Xmx256m -Xmx256m -XX:+HeapDumpOnOutOfMemoryError" -Dorg.gradle.daemon=false}"

export MAVEN_OPTS="${GUIDES_MAVEN_SYSTEM_PROPERTIES}"
export GRADLE_OPTS="${GUIDES_GRADLE_SYSTEM_PROPERTIES}"
export JAVA_TOOL_OPTIONS="${MAVEN_OPTS}"

cd "$(dirname $0)"

cd ../complete

./mvnw clean install
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf target

./gradlew build
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build

cd ../initial

./mvnw clean compile
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf target

./gradlew compileJava
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build

exit
