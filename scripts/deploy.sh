#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi


./mvnw clean install && (cd *-gradle-plugin; ./gradlew clean build install) && ./scripts/runTests.sh && ./mvnw deploy && (cd *-gradle-plugin; ./gradlew uploadArchives -x test)
