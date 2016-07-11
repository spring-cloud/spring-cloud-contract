#!/usr/bin/env bash

set -o errexit

./mvnw clean install
(cd *-gradle-plugin; ./gradlew clean build --parallel)
