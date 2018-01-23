FROM anapsix/alpine-java:8_jdk

MAINTAINER Marcin Grzejszczak <mgrzejszczak@pivotal.io>

VOLUME /tmp
ADD target/libs/stub-runner-boot.jar stub-runner-boot.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/stub-runner-boot.jar"]
