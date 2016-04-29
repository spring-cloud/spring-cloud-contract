package io.codearte.accurest.stubrunner

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubConfigurationSpec extends Specification {

	def 'should parse ivy notation'() {
		given:
			String ivy = 'group:artifact:version:classifier'
		when:
			StubConfiguration stubConfiguration = new StubConfiguration(ivy)
		then:
			stubConfiguration.artifactId == 'artifact'
			stubConfiguration.groupId == 'group'
			stubConfiguration.classifier == 'classifier'
			stubConfiguration.version == 'version'
	}
}
