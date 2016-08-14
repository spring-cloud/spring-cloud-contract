#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

[[ -z "${VERSION_VALUE}" ]] && VERSION_VALUE="1.0.0.BUILD-SNAPSHOT"
[[ -z "${VERIFIER_VERSION}" ]] && VERIFIER_VERSION="$VERSION_VALUE"
export VERIFIER_VERSION

echo -e "\n\nRUNNING TESTS FOR VERIFIER IN VERSION [${VERIFIER_VERSION}]\n\n"

cd samples/standalone

echo "Running tests"
../../mvnw clean install -P integration

cd $ROOT_FOLDER
