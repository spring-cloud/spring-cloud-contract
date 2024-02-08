#!/bin/bash

# A quick wrapper to source the gvm env and run it
# This is used to run gvm in Dockerfile RUN commands
# since the RUN commands does not use interactive shells

[[ -s "/home/scc/.sdkman/bin/sdkman-init.sh" ]] && source "/home/scc/.sdkman/bin/sdkman-init.sh" && sdk $@
