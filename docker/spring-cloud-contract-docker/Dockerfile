FROM ubuntu:20.04

ARG SDKMAN_JAVA_INSTALLATION=8.0.292.hs-adpt

LABEL Author="Marcin Grzejszczak <mgrzejszczak@pivotal.io>"
LABEL Author="Bastian Doetsch <bdoetsch@vmware.com>"

RUN apt-get update && apt-get install -y curl \
    unzip \
    zip \
    && apt-get clean

RUN useradd -u1000 -m scc

# Install sdkman and java
COPY sdkman/ /usr/local/bin/
RUN chmod +x /usr/local/bin/sdkman-wrapper.sh

# provide output & contract directories and change owner to scc
RUN mkdir -p /contracts
RUN mkdir -p /spring-cloud-contract-output
RUN mkdir -p /spring-cloud-contract
RUN chown -R scc:scc /contracts
RUN chown -R scc:scc /spring-cloud-contract-output /spring-cloud-contract
RUN chown -R scc:scc /spring-cloud-contract

USER scc
RUN curl -s https://get.sdkman.io/ | bash
COPY --chown=scc:scc sdkman.config /home/scc/.sdkman/etc/config
RUN bash -c "source /home/scc/.sdkman/bin/sdkman-init.sh"
RUN bash -c "sdkman-wrapper.sh install java ${SDKMAN_JAVA_INSTALLATION}"
ENV JAVA_HOME /home/scc/.sdkman/candidates/java/current/
ENV PATH "${PATH}:${JAVA_HOME}/bin"

# Spring Cloud Contract
COPY --chown=scc:scc project /spring-cloud-contract/
WORKDIR /spring-cloud-contract/

# Let's copy the downloaded deps to .m2 and the gradle cache
COPY --chown=scc:scc target/maven_dependencies /home/scc/.m2/repository/
COPY --chown=scc:scc target/gradle_dependencies /home/scc/.gradle/

# Let's download gradle wrapper if for some reason it hasn't been downloaded
RUN ./gradlew clean resolveDependencies build --stacktrace -x copyOutput || echo "Expected to fail"
CMD ["./build.sh"]
