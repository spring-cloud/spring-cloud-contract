stub-runner-junit
=================

Contains a JUnit Rule for Stub Runner.

Example of usage:

```
class AccurestRuleSpec extends Specification {

	@ClassRule @Shared AccurestRule rule = new AccurestRule()
			.repoRoot(AccurestRuleSpec.getResource("/m2repo").path)
			.downloadStub("io.codearte.accurest.stubs", "loanIssuance")
			.downloadStub("io.codearte.accurest.stubs:fraudDetectionServer")

	def 'should start WireMock servers'() {
		expect:
		rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance') != null
		rule.findStubUrl('loanIssuance') != null
		rule.findStubUrl('loanIssuance') == rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance')
		rule.findStubUrl('io.codearte.accurest.stubs:fraudDetectionServer') != null
	}
}
```

You can set the default value of the Maven repository by means of a system property:

```
-Dstubrunner.stubs.repository.root=http://your.maven.repo.com
```

The list of configurable properties contains:

| Name | Default value | Description |
|------|---------------|-------------|
| stubrunner.port.range.min | 10000 | Minimal value of a port for a WireMock server |
| stubrunner.port.range.max | 15000 | Maximum value of a port for a WireMock server |
| stubrunner.stubs.repository.root |  | Address to your M2 repo (will point to local M2 repo if none is provided) |
| stubrunner.stubs.repository.root |  | Address to your M2 repo (will point to local M2 repo if none is provided) |
| stubrunner.work-offline | false | Should try to connect to any repo to download stubs (especially good if there's no internet) |
| stubrunner.stubs | | Default comma separated list of stubs to download |

