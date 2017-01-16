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

class StubRepositorySpec extends Specification {
	public static
	final File REPOSITORY_LOCATION = new File('src/test/resources/repository')

	def 'should retrieve all descriptors for given project'() {
		given:
		StubRepository repository = new StubRepository(REPOSITORY_LOCATION)
		int expectedDescriptorsSize = 8
		when:
		List<WiremockMappingDescriptor> descriptors = repository.getProjectDescriptors()
		then:
		descriptors.size() == expectedDescriptorsSize
	}

	def 'should return empty list if files are missing'() {
		given:
		StubRepository repository = new StubRepository(new File('src/test/resources/emptyrepo'))
		when:
		List<WiremockMappingDescriptor> descriptors = repository.getProjectDescriptors()
		then:
		descriptors.empty
	}

	def 'should throw an exception if directory with mappings is missing'() {
		when:
		new StubRepository(new File('src/test/resources/nonexistingrepo'))
		then:
		thrown(IllegalArgumentException)
	}
}
