#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

export PROJECT_NAME="${PROJECT_NAME:-example}"
export DEBUG="${DEBUG:-false}"
echo "Setting project name to [${PROJECT_NAME}]"
echo "rootProject.name='${PROJECT_NAME}'" >> settings.gradle
echo "Running the build"
export ADDITIONAL_FLAGS="${ADDITIONAL_FLAGS:-}"
if [[ "${DEBUG}" == "true" ]]; then
  ADDITIONAL_FLAGS="${ADDITIONAL_FLAGS} --debug"
fi
./gradlew clean build publishToMavenLocal publish --stacktrace ${ADDITIONAL_FLAGS}
