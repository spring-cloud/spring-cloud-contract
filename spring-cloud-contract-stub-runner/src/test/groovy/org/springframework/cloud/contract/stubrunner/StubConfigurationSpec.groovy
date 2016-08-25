/*
 *  Copyright 2013-2016 the original author or authors.
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

	def 'should return ivy notation'() {
		given:
			String ivy = 'group:artifact:version:classifier'
		when:
			StubConfiguration stubConfiguration = new StubConfiguration(ivy)
		then:
			stubConfiguration.toColonSeparatedDependencyNotation() == ivy
	}
}
