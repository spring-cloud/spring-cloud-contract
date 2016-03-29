stub-runner-spring-cloud
========================

Registers the stubs in the provided Service Discovery. It's enough to add the jar

```
io.codearte.accurest:stub-runner-spring-cloud
```

and the Stub Runner autoconfiguration should be picked up.

In order to find the registered server via service discovery, you have to use the 
`artifactId` as the name of the application