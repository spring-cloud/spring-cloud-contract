#!/usr/bin/env bash

set -o errexit

mkdir -p build
GRADLE_OUTPUT=`./gradlew cV --quiet`
ACCUREST_VERSION=`echo ${GRADLE_OUTPUT##*:}`
export ACCUREST_VERSION=${ACCUREST_VERSION}

echo "Current accurest version is ${ACCUREST_VERSION}"

cd build
echo "Cloning samples"
git clone https://github.com/Codearte/accurest-samples
cd accurest-samples

echo "Running Gradle tests"
. ./runTests.sh

echo "Running Maven tests"
. ./runMavenTests.sh
