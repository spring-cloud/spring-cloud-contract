[![Build Status](https://travis-ci.org/Codearte/accurest-maven-plugin.svg?branch=master)](https://travis-ci.org/Codearte/accurest-maven-plugin) [![Coverage Status](https://coveralls.io/repos/github/Codearte/accurest-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/Codearte/accurest-maven-plugin?branch=master) [![GitHub issues](https://img.shields.io/github/issues/Codearte/accurest.svg)](https://github.com/Codearte/accurest/labels/maven) [![Maven Central](https://img.shields.io/maven-central/v/io.codearte.accurest/accurest-maven-plugin.svg)]()

Accurate REST Maven Plugin
====


Converting [Accurest](https://github.com/Codearte/accurest/wiki/1.-Introduction) GroovyDSL into WireMock stub mappings:

    mvn io.codearte.accurest:accurest-maven-plugin:convert
    
or shortly

    mvn accurest:convert
    
but this requires additional configuration inside `~/.m2/settings.xml`

```xml
<settings>
  <pluginGroups>
    <pluginGroup>io.codearte.accurest</pluginGroup>
  </pluginGroups>
</settings>

```


For more information please go to the [Wiki](https://github.com/Codearte/accurest/wiki/2.2-Maven-Project)
