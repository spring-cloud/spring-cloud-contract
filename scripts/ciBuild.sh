#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

FOLDER=`pwd`

set -e

./scripts/noIntegration.sh -Pfast
./scripts/gradleOnly.sh
./scripts/integrationOnly.sh
