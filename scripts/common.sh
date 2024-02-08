#!/usr/bin/env bash

set -e

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

echo "Root folder is $ROOT_FOLDER"

# Code grepping for the 3rd presence of "version" in pom.xml.
# The 3rd one is where we define the SC-Contract version
VERSION_NODE=`awk '/version/{i++}i==3{print; exit}' $ROOT_FOLDER/pom.xml`
# Extract the contents of the version node
VERSION_VALUE=$(sed -ne '/version/{s/.*<version>\(.*\)<\/version>.*/\1/p;q;}' <<< "$VERSION_NODE")

echo "Extracted version from root pom.xml is [$VERSION_VALUE]"

export ROOT_FOLDER
export VERSION_VALUE