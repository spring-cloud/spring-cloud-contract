#!/usr/bin/env bash

./mvnw deploy && (cd *-gradle-plugin; ./gradlew clean build install) && ./scripts/runTests.sh && (cd *-gradle-plugin; ./gradlew uploadArchives -x test)
