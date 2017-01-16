#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

[[ -z "${CORES}" ]] && CORES=8
echo -e "\n\nRUNNING PARALLEL BUILD WITH [${CORES}] CORES\n\n"

./mvnw clean install -Pdocs -T ${CORES} && ./mvnw install -Pintegration -T ${CORES} -Dinvoker.parallelThreads=${CORES} -pl :spring-cloud-contract-samples