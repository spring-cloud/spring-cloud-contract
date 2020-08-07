#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

echo "Running Spring Cloud Contract Stub Runner"
java -Djava.security.egd=file:/dev/./urandom -jar /stub-runner-boot.jar
