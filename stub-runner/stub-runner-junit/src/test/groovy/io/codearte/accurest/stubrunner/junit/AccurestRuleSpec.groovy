package io.codearte.accurest.stubrunner.junit

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class AccurestRuleSpec extends Specification {

	@BeforeClass
	@AfterClass
	void setupProps() {
		System.getProperties().setProperty("stubrunner.stubs.repository.root", "");
		System.getProperties().setProperty("stubrunner.stubs.classifier", "stubs");
	}

	// tag::classrule[]
	@ClassRule @Shared AccurestRule rule = new AccurestRule()
			.repoRoot(AccurestRuleSpec.getResource("/m2repo").toURI().toString())
			.downloadStub("io.codearte.accurest.stubs", "loanIssuance")
			.downloadStub("io.codearte.accurest.stubs:fraudDetectionServer")

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance') != null
			rule.findStubUrl('loanIssuance') != null
			rule.findStubUrl('loanIssuance') == rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance')
			rule.findStubUrl('io.codearte.accurest.stubs:fraudDetectionServer') != null
		and:
			rule.findAllRunningStubs().isPresent('loanIssuance')
			rule.findAllRunningStubs().isPresent('io.codearte.accurest.stubs', 'fraudDetectionServer')
			rule.findAllRunningStubs().isPresent('io.codearte.accurest.stubs:fraudDetectionServer')
		and: 'Stubs were registered'
			"${rule.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${rule.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}
	// end::classrule[]
}
