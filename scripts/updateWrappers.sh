#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

export WRAPPER_VERSION="${WRAPPER_VERSION:-7.3.3}"

find . -name 'build.gradle' | while read -r file; do
    parentdir="$(dirname "$file")"
    ( echo "Updating [${parentdir}]" && cd "$parentdir" && ./gradlew wrapper --gradle-version "${WRAPPER_VERSION}" )
done