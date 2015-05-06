Accurate REST
=============

[![Build Status](https://travis-ci.org/Codearte/accurest.svg?branch=master)](https://travis-ci.org/Codearte/accurest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin)

Consumer Driven Contracts verifier for Java

Just to make long story short - AccuREST is a tool for Consumer Driven Contract (CDC) development. AccuREST ships easy DSL for describing REST contracts for JVM-based applications. The contract DSL is used by AccuREST for two things:

generating Wiremock's JSON stub definitions, allowing rapid development of the consumer side,
generating Spock's acceptance tests for the server - to verify if your API implementation is compliant with the contract.
By using AccuREST you can move TDD to an architecture level.

For more information please follow to the [Wiki](https://github.com/Codearte/accurest/wiki/1.-Introduction)
