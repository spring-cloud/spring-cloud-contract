#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

./mvnw -s .settings.xml --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/spring-cloud-contact-gradle-plugin && ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/samples-standalone && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/samples-standalone && ./downloadGradleDeps.sh
