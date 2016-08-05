# Spring Cloud Contract

Feature ideas:

- [x] Move DSL and stub runner code over from [Accurest](https://github.com/Codearte/accurest)
- [x] Spring Restdocs generators
- [ ] RAML generators
- [ ] Named scenarios for modelling sequences of request-response with state changes
- [x] Wiremock support for Spring Boot apps
- [x] Wiremock mock servers using `MockRestServiceServer` (from Spring Test)
- [ ] POJO-based contracts (packaging DTOs and stub declarations from the producer)
- [ ] Sample messages packaged and injectable into Spring Cloud Stream consumer tests
- [ ] `DiscoveryClient` support so backends can be stubbed or mocked transparently (declaratively)
- [ ] Actuator endpoints that verify the state of contracts supported by the host service
- [ ] Something to make PACT easier to use in Spring Boot apps?
- [ ] Support [Citrus](http://www.citrusframework.org/) users (somehow?)
- [ ] Schema registries for binary formats like protobuf, thrift, avro
