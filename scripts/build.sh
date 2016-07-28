#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

ADDITIONAL_MAVEN_OPTS=${ADDITIONAL_MAVEN_OPTS:--Dmaven.test.redirectTestOutputToFile=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn}

echo "Building Maven and Gradle projects for version [$VERIFIER_VERSION]"

echo "Building Maven stuff with additional opts [$ADDITIONAL_MAVEN_OPTS]"
./mvnw clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Psonar --batch-mode $ADDITIONAL_MAVEN_OPTS
