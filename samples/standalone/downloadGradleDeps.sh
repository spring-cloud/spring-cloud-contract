#!/bin/bash

set -o errexit

LOCAL_MAVEN_REPO=${LOCAL_MAVEN_REPO-~/.m2}
VERIFIER_VERSION=${VERIFIER_VERSION:-1.0.0.BUILD-SNAPSHOT}
ROOT=`pwd`

cat <<EOF
Downloading deps
LOCAL_MAVEN_REPO=${LOCAL_MAVEN_REPO}
VERIFIER_VERSION=${VERIFIER_VERSION}
EOF

cd dsl/http-server
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION}
cd $ROOT
cd dsl/http-client
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT

cd restdocs/http-server
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION}
cd $ROOT
cd restdocs/http-client
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT

cd contract-verifier-sample-stream-source
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT
cd contract-verifier-sample-stream-sink
./gradlew resolveDependencies -PverifierVersion=${VERIFIER_VERSION} --stacktrace
cd $ROOT
