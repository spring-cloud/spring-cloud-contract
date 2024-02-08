#!/bin/bash

set -e

CURRENT_DIR="$( pwd )"
ADOC_OUTPUT_DIR="${CURRENT_DIR}/target/adoc/"
pushd project
  mkdir -p "${ADOC_OUTPUT_DIR}"
  ./gradlew dumpAllProps
  cp "$( pwd )/build/props.adoc" "${ADOC_OUTPUT_DIR}/"
  cp "$( pwd )/build/appProps.adoc" "${ADOC_OUTPUT_DIR}/"
popd
