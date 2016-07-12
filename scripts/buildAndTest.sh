#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

echo "Building all libraries"
$ROOT_FOLDER/scripts/build.sh

echo "Running tests"
$ROOT_FOLDER/scripts/runTests.sh
