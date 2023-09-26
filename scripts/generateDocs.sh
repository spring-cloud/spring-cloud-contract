#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

echo "Generating docs for Spring Cloud Contract"

echo "Building main docs"
./mvnw clean install -P docs --pl docs ${@}