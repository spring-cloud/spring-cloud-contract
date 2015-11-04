Accurate REST
=============

[![Build Status](https://travis-ci.org/Codearte/accurest.svg?branch=master)](https://travis-ci.org/Codearte/accurest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin)
[![Join the chat at https://gitter.im/Codearte/accurest](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Codearte/accurest?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Consumer Driven Contracts verifier for Java

To make a long story short - AccuREST is a tool for Consumer Driven Contract (CDC) development. AccuREST ships an easy DSL for describing REST contracts for JVM-based applications. The contract DSL is used by AccuREST for two things:

1. generating WireMock's JSON stub definitions, allowing rapid development of the consumer side,
generating Spock's acceptance tests for the server - to verify if your API implementation is compliant with the contract.
2. moving TDD to an architecture level.

For more information please go to the [Wiki](https://github.com/Codearte/accurest/wiki/1.-Introduction)

## Requirements

### Wiremock

In order to use Accurest with Wiremock you have to have __Wiremock in version at least 2.0.0-beta__ . Of course the higher the better :)
