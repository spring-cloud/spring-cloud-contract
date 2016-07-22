#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

function print_usage() {
cat <<EOF
USAGE:
You can use the following options:
-v|--version - which version of Spring Cloud Contract do you want to use
EOF
}

while [[ $# > 0 ]]
do
key="$1"
case $key in
    -v|--version)
    VERIFIER_VERSION="$2"
    shift # past argument
    ;;
    --help)
    print_usage
    exit 0
    ;;
    *)
    echo "Invalid option: [$1]"
    print_usage
    exit 1
    ;;
esac
shift # past argument or value
done

# Code grepping for the 3rd presence of "version" in pom.xml.
# The 3rd one is where we define the SC-Contract version
VERSION_NODE=`awk '/version/{i++}i==3{print; exit}' $ROOT_FOLDER/pom.xml`
# Extract the contents of the version node
VERSION_VALUE=$(sed -ne '/version/{s/.*<version>\(.*\)<\/version>.*/\1/p;q;}' <<< "$VERSION_NODE")

[[ -z "${VERIFIER_VERSION}" ]] && VERIFIER_VERSION="$VERSION_VALUE"
export VERIFIER_VERSION

echo -e "\n\nBUILDING AND RUNNING TESTS FOR VERIFIER IN VERSION [$VERIFIER_VERSION]\n\n"

echo "Building all libraries"
$ROOT_FOLDER/scripts/build.sh

echo "Running tests"
$ROOT_FOLDER/scripts/runTests.sh
