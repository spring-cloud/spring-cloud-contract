#!/bin/bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

echo "Running Spring Cloud Contract Stub Runner"
ADDITIONAL_OPTS="${ADDITIONAL_OPTS:-}"
MESSAGING_TYPE="${MESSAGING_TYPE:-}"

if [[ "${MESSAGING_TYPE}" != "" ]]; then
  echo "Messaging type passed, will active thin profile [${MESSAGING_TYPE}]"
  ADDITIONAL_OPTS="${ADDITIONAL_OPTS} --thin.profile=${MESSAGING_TYPE}"
fi

echo "Please wait for the dependencies to be downloaded..."
java -Djava.security.egd=file:/dev/./urandom -jar /home/scc/stub-runner-boot.jar ${ADDITIONAL_OPTS}
