FROM ubuntu:20.04

ARG SDKMAN_JAVA_INSTALLATION=17.0.1-tem
ARG THIN_REPO=https://repo.spring.io/snapshot

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

USER scc
WORKDIR /home/scc

RUN curl -s https://get.sdkman.io/ | bash
COPY --chown=scc sdkman.config /home/scc/.sdkman/etc/config
RUN bash -c "source /home/scc/.sdkman/bin/sdkman-init.sh"
RUN bash -c "sdkman-wrapper.sh install java ${SDKMAN_JAVA_INSTALLATION}"
ENV JAVA_HOME /home/scc/.sdkman/candidates/java/current/
ENV PATH "${PATH}:${JAVA_HOME}/bin"

# Spring Cloud Contract
ENV SERVER_PORT 8750
VOLUME /tmp
COPY --chown=scc:scc target/maven_dependencies /home/scc/.m2/repository/
COPY --chown=scc:scc target/libs/stub-runner-boot.jar stub-runner-boot.jar
RUN echo "Fetching dependencies, please wait..."
RUN java -Dthin.dryrun=true -jar stub-runner-boot.jar --thin.repo=${THIN_REPO}
RUN java -Dthin.dryrun=true -jar stub-runner-boot.jar --thin.profile=rabbit --thin.repo=${THIN_REPO}
RUN java -Dthin.dryrun=true -jar stub-runner-boot.jar --thin.profile=kafka --thin.repo=${THIN_REPO}
COPY --chown=scc:scc run.sh run.sh
ENTRYPOINT ["./run.sh"]
