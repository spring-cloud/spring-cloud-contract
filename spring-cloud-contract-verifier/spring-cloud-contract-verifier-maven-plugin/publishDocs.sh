#!/bin/sh
mvn clean site site:stage scm-publish:publish-scm -DskipTests -pl spring-cloud-contract-verifier-maven-plugin