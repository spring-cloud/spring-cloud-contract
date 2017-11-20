#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

echo "Generating docs for Spring Cloud Contract"

echo "Building main docs"
./mvnw clean install -P docs -DskipTests=true --pl docs ${@}

echo "Building maven plugin docs"
./mvnw site -DskipTests=true --pl spring-cloud-contract-tools/spring-cloud-contract-maven-plugin ${@}

echo "Copying generated maven plugin docs to main docs"
cp -r spring-cloud-contract-tools/spring-cloud-contract-maven-plugin/target/site docs/target/generated-docs/spring-cloud-contract-maven-plugin