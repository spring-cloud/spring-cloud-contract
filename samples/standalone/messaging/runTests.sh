#!/bin/bash

set -o errexit

VERIFIER_VERSION=$1
ROOT=`pwd`

cat <<EOF
Running tests with the following parameters
VERIFIER_VERSION=${VERIFIER_VERSION}
EOF

rm -rf ~/.m2/repository/com/example/

echo -e "\n\nRunning the tests via bash script only because we need to force order of execution."
echo -e "First server and client with Maven. Then server and client with Gradle."

echo -e "\n\nRunning tests for Maven (Messaging communication)\n\n"
echo -e "Building server"
cd ${ROOT}/stream-source
./mvnw clean install -Dspring-contract.version=${VERIFIER_VERSION}

echo -e "Building client"
cd ${ROOT}/stream-sink
./mvnw clean install -Dspring-contract.version=${VERIFIER_VERSION}

rm -rf ~/.m2/repository/com/example/

echo -e "\n\nRunning tests for Gradle (Messaging communication)\n\n"
echo -e "Building server"
cd ${ROOT}/stream-source
./mvnw clean install -DskipTests -Dspring-contract.version=${VERIFIER_VERSION} -P integration

echo -e "Building client"
cd ${ROOT}/stream-sink
./mvnw clean install -DskipTests -Dspring-contract.version=${VERIFIER_VERSION} -P integration