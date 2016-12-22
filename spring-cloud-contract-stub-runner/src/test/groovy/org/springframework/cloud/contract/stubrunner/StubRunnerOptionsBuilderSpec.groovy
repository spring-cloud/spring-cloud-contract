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

import spock.lang.Issue
import spock.lang.Specification

class StubRunnerOptionsBuilderSpec extends Specification {

	private StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()

	def shouldCreateDependenciesForStub() {

		given:
		builder.withStubs('foo:bar')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getDependencies().toString() == '[foo:bar:+:stubs]'
	}

	def shouldCreateDependenciesForCommaSeparatedStubs() {

		given:
		builder.withStubs('foo:bar,bar:foo')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getDependencies().size() == 2
	}

	def shouldMapStubsWithPort() {

		given:
		builder.withStubs('foo:bar:8080')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getStubIdsToPortMapping().toString() == '[foo:bar:+:stubs:8080]'
	}

	def shouldCreateDependenciesWithVersion() {

		given:
		builder.withStubs('foo:1.2.3:bar')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getDependencies().toString() == '[foo:1.2.3:bar:stubs]'
	}

	def shouldCreateDependenciesWithClassifier() {

		given:
		builder.withStubs('foo:bar').withStubsClassifier('xxx')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getDependencies().toString() == '[foo:bar:+:xxx]'
	}

	@Issue("#176")
	def shouldCreateDependenciesWithEmptyPort() {

		given:
		builder.withStubs('groupId:artifactId:version:classifier:')

		when:
		StubRunnerOptions options = builder.build()

		then:
		options.getDependencies().toString() == '[groupId:artifactId:version:classifier]'
	}
}
