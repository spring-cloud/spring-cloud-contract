#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

./mvnw clean install && (cd *-gradle-plugin; ./gradlew clean build install) && ./scripts/runTests.sh && ./mvnw deploy && (cd *-gradle-plugin; ./gradlew uploadArchives -x test)
