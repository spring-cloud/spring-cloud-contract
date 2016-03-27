stub-runner-spring
=======================

Sets up Spring configuration of the Stub Runner project.

By providing a list of stubs inside your configuration file the Stub Runner automatically downloads 
and registers in WireMock the selected stubs.

If you want to find the URL of your stubbed dependency you can autowire the `StubFinder` interface and use
its methods as presented below:

```
	@Autowired StubFinder stubFinder

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			stubFinder.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') == stubFinder.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance')
			stubFinder.findStubUrl('io.codearte.accurest.stubs:fraudDetectionServer') != null
		and:
			stubFinder.findAllRunningStubs().isPresent('loanIssuance')
			stubFinder.findAllRunningStubs().isPresent('io.codearte.accurest.stubs', 'fraudDetectionServer')
			stubFinder.findAllRunningStubs().isPresent('io.codearte.accurest.stubs:fraudDetectionServer')
		and: 'Stubs were registered'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}
```

for the following configuration file:

```
stubrunner.stubs.repository.root: classpath:m2repo
stubrunner.stubs: io.codearte.accurest.stubs:loanIssuance,io.codearte.accurest.stubs:fraudDetectionServer
```