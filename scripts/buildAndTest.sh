#!/usr/bin/env bash

set -o errexit

ROOT_FOLDER=`pwd`
echo "Current folder is $ROOT_FOLDER"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
fi

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

[[ -z "${VERIFIER_VERSION}" ]] && VERIFIER_VERSION="1.0.0.BUILD-SNAPSHOT"
export VERIFIER_VERSION

echo -e "\n\nBUILDING AND RUNNING TESTS FOR VERIFIER IN VERSION [$VERIFIER_VERSION]\n\n"

echo "Building all libraries"
$ROOT_FOLDER/scripts/build.sh

echo "Running tests"
$ROOT_FOLDER/scripts/runTests.sh
