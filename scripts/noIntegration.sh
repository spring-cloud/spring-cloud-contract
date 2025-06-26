#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

FOLDER=`pwd`

set -e

./mvnw clean dokka:javadocJar install ${@}
${FOLDER}/scripts/generateDocs.sh ${@}