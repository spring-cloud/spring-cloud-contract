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
   
Publishing wiremock stubs (projectF-stubs.jar)
---

Project configuration

```xml
<plugin>
    <groupId>io.codearte.accurest</groupId>
    <artifactId>accurest-maven-plugin</artifactId>
    <version>${accurest.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generateStubs</goal>
            </goals>
        </execution>
    </executions>
</plugin>
<plugin>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>2.6</version>
    <dependencies>
        <dependency>
            <groupId>io.codearte.accurest</groupId>
            <artifactId>stubs-assembly-descriptor</artifactId>
            <version>${accurest.version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <descriptorRefs>
            <descriptorRef>stubs</descriptorRef>
        </descriptorRefs>
    </configuration>
</plugin>
```

Deploy to artifact repository using `mvn deploy`
