#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

./mvnw -s .settings.xml --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/spring-cloud-contact-gradle-plugin && ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-client && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-client && ./downloadGradleDeps.sh
cd $ROOT_FOLDER/samples/standalone/dsl/http-server && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-server && ./downloadGradleDeps.sh
cd $ROOT_FOLDER/samples/standalone/restdocs/http-client && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-client && ./downloadGradleDeps.sh
cd $ROOT_FOLDER/samples/standalone/restdocs/http-server && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-server && ./downloadGradleDeps.sh
cd $ROOT_FOLDER/samples/standalone/messaging/stream-sink && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/messaging/stream-sink && ./downloadGradleDeps.sh
cd $ROOT_FOLDER/samples/standalone/messaging/sream-source && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/messaging/sream-source && ./downloadGradleDeps.sh
