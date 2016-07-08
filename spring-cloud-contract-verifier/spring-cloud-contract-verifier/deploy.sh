#!/usr/bin/env bash

./gradlew clean build install && ./scripts/runTests.sh && ./gradlew uploadArchives -x test