[![Build Status](https://travis-ci.org/Codearte/accurest-maven-plugin.svg?branch=master)](https://travis-ci.org/Codearte/accurest-maven-plugin) [![Coverage Status](https://coveralls.io/repos/github/Codearte/accurest-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/Codearte/accurest-maven-plugin?branch=master)

Accurest Maven Plugin
====

Sample usage
----

    <plugin>
        <groupId>io.codearte.accurest</groupId>
        <artifactId>accurest-maven-plugin</artifactId>
        <executions>
            <execution>
                <goals>
                    <goal>generateStubs</goal>
                    <goal>generateSpecs</goal>
                </goals>
            </execution>
        </executions>
    </plugin>