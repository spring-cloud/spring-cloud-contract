#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

[[ -z "${CORES}" ]] && CORES=1
echo -e "\n\nRUNNING FAST BUILD (NO INTEGRATION TESTS) WITH [${CORES}] CORES\n\n"

./mvnw clean install -Pdocs,fast -T ${CORES}