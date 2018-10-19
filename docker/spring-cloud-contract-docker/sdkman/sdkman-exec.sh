#!/bin/bash

# A quick wrapper to source the gvm env and run a command
# This is used to run sdkman in Dockerfile RUN commands
# since the RUN commands does not use interactive shells

[[ -s "/root/.sdkman/bin/sdkman-init.sh" ]] && source "/root/.sdkman/bin/sdkman-init.sh" && exec "$@"