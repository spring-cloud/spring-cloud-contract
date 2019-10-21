FROM ubuntu:18.10

ARG SDKMAN_JAVA_INSTALLATION=8.0.222.hs-adpt

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
COPY project /spring-cloud-contract/
WORKDIR /spring-cloud-contract/
# Let's copy the downloaded deps to .m2 and the gradle cache
COPY target/maven_dependencies /root/.m2/repository/
COPY target/gradle_dependencies /root/.gradle/
# Let's download gradle wrapper if for some reason it hasn't been downloaded
RUN ./gradlew clean resolveDependencies build --stacktrace -x copyOutput || echo "Expected to fail"
CMD ["./build.sh"]
