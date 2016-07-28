#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

./mvnw clean install && ./scripts/runTests.sh && ./mvnw deploy
