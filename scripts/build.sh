#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

ADDITIONAL_MAVEN_OPTS=${ADDITIONAL_MAVEN_OPTS:--Dmaven.test.redirectTestOutputToFile=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn}

echo "Building Maven stuff with additional opts [$ADDITIONAL_MAVEN_OPTS]"
./mvnw clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Psonar --batch-mode $ADDITIONAL_MAVEN_OPTS

echo "Building Gradle plugin"
(cd */*-gradle-plugin; ./gradlew clean build install jacocoTestReport)
