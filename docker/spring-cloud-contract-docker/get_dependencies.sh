#!/bin/bash

set -e

WRAPPER_VERSION="4.4.1"
GRADLE_BIN_DIR="gradle-${WRAPPER_VERSION}-bin"
GRADLE_WRAPPER_DIR="${HOME}/.gradle/wrapper/dists/${GRADLE_BIN_DIR}"
CURRENT_DIR="$( pwd )"
GRADLE_OUTPUT_DIR="${CURRENT_DIR}/target/gradle_dependencies/"
pushd project
rm -rf .gradle
./gradlew wrapper --gradle-version "${WRAPPER_VERSION}"
./gradlew clean build -g "${GRADLE_OUTPUT_DIR}" -x copyOutput || echo "Expected to fail the build"
if [ -d "${GRADLE_WRAPPER_DIR}" ]; then
    echo "Copying Gradle Wrapper version [${WRAPPER_VERSION}]"
    mkdir -p "${GRADLE_OUTPUT_DIR}/wrapper/dists/"
    cp -r "${GRADLE_WRAPPER_DIR}" "${GRADLE_OUTPUT_DIR}/wrapper/dists/"
else
    echo "Gradle Wrapper [${GRADLE_WRAPPER_DIR}] not found. Will not copy it"
fi
rm -rf build
popd
