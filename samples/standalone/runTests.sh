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

echo -e "\n\nRunning tests for Gradle (HTTP communication)\n\n"
echo -e "Building server (uses Spring Cloud Contract Verifier Gradle Plugin)"
cd http-server
./gradlew clean build publishToMavenLocal -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT
echo -e "\n\nBuilding client (uses Spring Cloud Contract Stub Runner)"
cd http-client
./gradlew clean build -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT

echo -e "\n\nClearing saved stubs"
rm -rf $LOCAL_MAVEN_REPO/repository/org/springframework/cloud/contract/testprojects/

echo -e "\n\nRunning tests for Gradle (communication via messaging)\n\n"
echo -e "Building producer (uses Spring Cloud Contract Verifier Gradle Plugin)"
cd stream-source
./gradlew clean build publishToMavenLocal -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT
echo -e "\n\nBuilding consumer (uses Spring Cloud Contract Stub Runner Messaging)"
cd stream-sink
./gradlew clean build -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT
