#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

mkdir -p target
CONTRACT_VERIFIER_VERSION=1.0.0.BUILD-SNAPSHOT
export CONTRACT_VERIFIER_VERSION=${CONTRACT_VERIFIER_VERSION}

echo "Current Spring Cloud Contract Verifier version is ${CONTRACT_VERIFIER_VERSION}"

cd target
echo "Cloning samples"
rm -rf spring-cloud-contract-verifier-samples
git clone https://github.com/spring-cloud-samples/spring-cloud-contract-verifier-samples
cd spring-cloud-contract-verifier-samples

echo "Running Gradle tests"
. ./runTests.sh

echo "Running Maven tests"
. ./runMavenTests.sh

cd $ROOT_FOLDER