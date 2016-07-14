#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

./mvnw -s .settings.xml --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/spring-cloud-contact-gradle-plugin && ./gradlew resolveDependencies || true
cd $ROOT_FOLDER/samples/samples-standalone && ./mvnw --fail-never dependency:go-offline || true
cd $ROOT_FOLDER/samples/samples-standalone && ./downloadGradleDeps.sh
