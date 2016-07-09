#!/usr/bin/env bash

./mvnw install
(cd *-gradle-plugin; ./gradlew clean build --parallel)
