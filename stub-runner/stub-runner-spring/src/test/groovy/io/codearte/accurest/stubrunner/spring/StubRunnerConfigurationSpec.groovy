package io.codearte.accurest.stubrunner.spring

import io.codearte.accurest.stubrunner.StubFinder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
@ContextConfiguration(classes = Config, loader = SpringApplicationContextLoader)
class StubRunnerConfigurationSpec extends Specification {

	@Autowired StubFinder stubFinder

	def 'should start WireMock servers'() {
		expect: 'WireMocks are running'
			stubFinder.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') != null
			stubFinder.findStubUrl('loanIssuance') == stubFinder.findStubUrl('io.codearte.accurest.stubs', 'loanIssuance')
			stubFinder.findStubUrl('io.codearte.accurest.stubs:fraudDetectionServer') != null
		and: 'Stubs were registered'
			"${stubFinder.findStubUrl('loanIssuance').toString()}/name".toURL().text == 'loanIssuance'
			"${stubFinder.findStubUrl('fraudDetectionServer').toString()}/name".toURL().text == 'fraudDetectionServer'
	}

	@Configuration
	@Import(StubRunnerConfiguration)
	@EnableAutoConfiguration
	static class Config {

	}
}
