#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

echo "Building main docs"
./mvnw clean install -P docs -DskipTests=true --pl docs

echo "Building maven plugin docs"
./mvnw site site:stage -DskipTests=true --pl spring-cloud-contract-verifier-maven-plugin

echo "Copying generated maven plugin docs to main docs"
cp -r spring-cloud-contract-verifier-maven-plugin/target/site docs/target/generated-docs/spring-cloud-contract-verifier-maven-plugin