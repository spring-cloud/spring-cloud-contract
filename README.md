[![Build Status](https://travis-ci.org/Codearte/accurest-maven-plugin.svg?branch=master)](https://travis-ci.org/Codearte/accurest-maven-plugin) [![Coverage Status](https://coveralls.io/repos/github/Codearte/accurest-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/Codearte/accurest-maven-plugin?branch=master)

Accurate REST Maven Plugin
====

Sample usage
----

```xml
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
```


Project default directories
---

_contractsDir_ - contract definitions:

    /src/test/resources/contracts

_generatedTestSourcesDir_ - Generated accurest specifications:

    /target/generated-test-sources/accurest
