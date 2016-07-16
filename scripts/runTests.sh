#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

export CONTRACT_VERIFIER_VERSION=${CONTRACT_VERIFIER_VERSION:-1.0.0.BUILD-SNAPSHOT}

echo "Current Spring Cloud Contract Verifier version is ${CONTRACT_VERIFIER_VERSION}"

cd samples/standalone

echo "Running Gradle tests"
. ./runTests.sh

echo "Running Maven tests"
. ./runMavenTests.sh

cd $ROOT_FOLDER
