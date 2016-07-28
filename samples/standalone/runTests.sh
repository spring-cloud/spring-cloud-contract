#!/bin/bash

set -o errexit

LOCAL_MAVEN_REPO=${LOCAL_MAVEN_REPO-~/.m2}
VERIFIER_VERSION=${VERIFIER_VERSION:-1.0.0.BUILD-SNAPSHOT}
ROOT=`pwd`

cat <<EOF
Running tests with the following parameters
LOCAL_MAVEN_REPO=${LOCAL_MAVEN_REPO}
VERIFIER_VERSION=${VERIFIER_VERSION}
EOF

echo -e "\n\nClearing saved stubs"
rm -rf $LOCAL_MAVEN_REPO/repository/org/springframework/cloud/contract/testprojects/
rm -rf $LOCAL_MAVEN_REPO/repository/com/example

echo -e "\n\nRunning tests for Maven (HTTP communication)\n\n"
echo -e "Building server (uses Spring Cloud Contract Verifier Maven Plugin)"
./mvnw clean install -Dspring-contract.version=${VERIFIER_VERSION} -P integration
