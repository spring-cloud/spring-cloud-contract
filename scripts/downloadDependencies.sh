#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

./mvnw -s .settings.xml --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/spring-cloud-contact-gradle-plugin; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-client; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-client; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-server; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/dsl/http-server; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-client; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-client; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-server; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/restdocs/http-server; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/messaging/stream-sink; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/messaging/stream-sink; ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/standalone/messaging/stream-source; ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/standalone/messaging/stream-source; ./gradlew resolveDependencies || true
