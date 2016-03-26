package io.codearte.accurest.stubrunner.junit

import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

/**
 * @author Marcin Grzejszczak
 */
@RestoreSystemProperties
class AccurestRuleSysPropsSpec extends Specification {

	static {
		System.properties.setProperty("stubrunner.stubs.repository.root", AccurestRuleSysPropsSpec.getResource("/m2repo").path)
		System.properties.setProperty("stubrunner.stubs.classifier", 'classifier that will be overridden')
	}

	@ClassRule @Shared AccurestRule rule = new AccurestRule()
			.downloadStub("io.codearte.accurest.stubs", "loanIssuance", "stubs")
			.downloadStub("io.codearte.accurest.stubs:fraudDetectionServer:stubs")

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance') != null
			rule.findStubUrl('loanIssuance') != null
			rule.findStubUrl('loanIssuance') == rule.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance')
			rule.findStubUrl('io.codearte.accurest.stubs:fraudDetectionServer') != null
		and: 'Stubs were registered'
			"${rule.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${rule.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}
}
