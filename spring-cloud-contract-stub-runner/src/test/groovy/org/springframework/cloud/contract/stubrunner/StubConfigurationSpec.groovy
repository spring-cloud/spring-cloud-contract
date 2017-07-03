/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Grzejszczak
 */
class StubConfigurationSpec extends Specification {

	@Unroll
	def 'should parse (#ivy) and match ivy notation'() {
		when:
			StubConfiguration stubConfiguration = new StubConfiguration(ivy)
		then:
			stubConfiguration.artifactId == artifactId
			stubConfiguration.groupId == groupId
			stubConfiguration.classifier == classifier
			stubConfiguration.version == version
		and:
			stubConfiguration.toColonSeparatedDependencyNotation() == ivyNotation
		and:
			stubConfiguration.matchesIvyNotation(ivy)
		where:
			ivy                                 || ivyNotation                         | artifactId | groupId | classifier   | version
			'group:artifact:version:classifier' || 'group:artifact:version:classifier' | 'artifact' | 'group' | 'classifier' | 'version'
			'group:artifact:version:'           || 'group:artifact:version:'           | 'artifact' | 'group' | ''           | 'version'
			'group:artifact:version'            || 'group:artifact:version:stubs'      | 'artifact' | 'group' | 'stubs'      | 'version'

	}

	@Unroll
	def 'should resolve [#ivy] as a changing version [#result]'() {
		given:
			StubConfiguration stubConfiguration = new StubConfiguration(ivy)
		expect:
			result == stubConfiguration.isVersionChanging()
		where:
			ivy                                       || result
			'group:artifact:1.0.0.RELEASE:classifier' || false
			'group:artifact:1.0.0.BUILD-SNAPSHOT:'    || true
			'group:artifact:1.0.0.SNAPSHOT'           || true
			'group:artifact:+:'                       || true

	}

}
