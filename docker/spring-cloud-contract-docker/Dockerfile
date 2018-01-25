FROM anapsix/alpine-java:8_jdk

MAINTAINER Marcin Grzejszczak <mgrzejszczak@pivotal.io>

COPY project /spring-cloud-contract/
WORKDIR /spring-cloud-contract/
# Let's copy the downloaded deps to .m2 and the gradle cache
COPY target/maven_dependencies /root/.m2/repository/
COPY target/gradle_dependencies /root/.gradle/
# Let's download gradle wrapper if for some reason it hasn't been downloaded
RUN ./gradlew clean build --stacktrace -x copyOutput || echo "Expected to fail"
CMD ["./build.sh"]
