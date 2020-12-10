FROM ubuntu:21.04

ARG SDKMAN_JAVA_INSTALLATION=8.0.275.hs-adpt

MAINTAINER Marcin Grzejszczak <mgrzejszczak@pivotal.io>

RUN apt-get update && apt-get install -y curl \
    unzip \
    zip \
    && apt-get clean

# Install sdkman and java
RUN curl -s https://get.sdkman.io/ | bash
COPY sdkman.config /.sdkman/etc/config
COPY sdkman/ /usr/local/bin/
RUN /bin/bash -c "chmod +x /usr/local/bin/sdkman-exec.sh && chmod +x /usr/local/bin/sdkman-wrapper.sh && chmod +x /root/.sdkman/bin/sdkman-init.sh"
RUN /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh"
RUN sdkman-wrapper.sh install java "${SDKMAN_JAVA_INSTALLATION}"
ENV JAVA_HOME /root/.sdkman/candidates/java/current/
ENV PATH "${PATH}:${JAVA_HOME}/bin"

# Spring Cloud Contract
ENV SERVER_PORT 8083
VOLUME /tmp
ADD target/libs/stub-runner-boot.jar stub-runner-boot.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/stub-runner-boot.jar"]
