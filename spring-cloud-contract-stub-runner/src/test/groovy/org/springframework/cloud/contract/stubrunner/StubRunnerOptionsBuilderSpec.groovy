/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner

import spock.lang.Issue
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource

class StubRunnerOptionsBuilderSpec extends Specification {

	private StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()

	def shouldReturnURIOfAResourceFromString() {

		given:
			builder.withStubRepositoryRoot("classpath:/logback.xml")

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getStubRepositoryRootAsString().startsWith("file:/")
			options.getStubRepositoryRootAsString().endsWith("logback.xml")
	}

	def shouldReturnURIOfAResourceFromResource() {

		given:
			builder.withStubRepositoryRoot(new ClassPathResource("logback.xml"))

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getStubRepositoryRootAsString().startsWith("file:/")
			options.getStubRepositoryRootAsString().endsWith("logback.xml")
	}

	def shouldReturnEmptyStringWhenFileNotFound() {

		given:
			builder.withStubRepositoryRoot(new ClassPathResource("fileThatDoesNotExist.xml"))

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getStubRepositoryRootAsString() == ""
	}

	def shouldCreateDependenciesForStub() {

		given:
			builder.withStubs('foo:bar')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[foo:bar:+:stubs]'
	}

	def shouldCreateDependenciesForStubWithSameGroupAndArtifactId() {

		given:
			builder.withStubs('foo:bar:1.0', 'foo:bar:2.0')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[foo:bar:1.0:stubs, foo:bar:2.0:stubs]'
	}

	def shouldCreateDependenciesForMultipleStubsWithSameGroup() {

		given:
			builder.withStubs('foo:bar', 'foo:baz', 'foo:baz2')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[foo:bar:+:stubs, foo:baz:+:stubs, foo:baz2:+:stubs]'
	}

	def shouldCreateDependenciesForMultipleStubsWithSameArtifactId() {

		given:
			builder.withStubs('bar:foo', 'baz:foo', 'baz2:foo')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[bar:foo:+:stubs, baz:foo:+:stubs, baz2:foo:+:stubs]'
	}

	def shouldCreateDependenciesForCommaSeparatedStubs() {

		given:
			builder.withStubs('foo:bar,bar:foo,foo:baz')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().size() == 3
	}

	def shouldCreateDependenciesForCommaSeparatedStubsWithSameArtifact() {

		given:
			builder.withStubs('bar:foo,baz:foo,baz2:foo')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[bar:foo:+:stubs, baz:foo:+:stubs, baz2:foo:+:stubs]'
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

	@Issue("#210")
	def shouldCreateDependenciesWithVersionRange() {

		given:
			builder.withStubs('groupId:artifactId:[,0.0.1]:classifier', 'groupId2:artifactId2:[,0.0.2]:classifier2')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[groupId:artifactId:[,0.0.1]:classifier, groupId2:artifactId2:[,0.0.2]:classifier2]'
	}

	@Issue("#210")
	def shouldCreateDependenciesWithVersionRangeWhenSingleOneWasPassed() {

		given:
			builder.withStubs('groupId:artifactId:[,0.0.1]:classifier')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[groupId:artifactId:[,0.0.1]:classifier]'
	}

	@Issue("#210")
	def shouldCreateDependenciesWithVersionRangeWhenManyWerePassedInASingleLine() {

		given:
			builder.withStubs('groupId:artifactId:[,0.0.1]:classifier,groupId2:artifactId2:[,0.0.2]:classifier2')

		when:
			StubRunnerOptions options = builder.build()

		then:
			options.getDependencies().toString() == '[groupId2:artifactId2:[,0.0.2]:classifier2, groupId:artifactId:[,0.0.1]:classifier]'
	}

	@Issue("#466")
	def shouldSetAllDependenciesFromOptions() {
		given:
			StubRunnerOptionsBuilder builder = builder.withOptions(new StubRunnerOptions(1, 2, new FileSystemResource("root"), StubRunnerProperties.StubsMode.LOCAL,
					"classifier", [new StubConfiguration("a:b:c")], [(new StubConfiguration("a:b:c")): 3], "foo", "bar",
					new StubRunnerOptions.StubRunnerProxyOptions("host", 4), true, "consumer", "folder", false, true, false, [foo: "bar"], Foo, "server"))
			builder.withStubs("foo:bar:baz")
		when:
			StubRunnerOptions options = builder.build()
		then:
			options.minPortValue == 1
			options.maxPortValue == 2
			options.stubRepositoryRoot == new FileSystemResource("root")
			options.stubsMode == StubRunnerProperties.StubsMode.LOCAL
			options.stubsClassifier == "classifier"
			options.dependencies == [new StubConfiguration("a:b:c"), new StubConfiguration("foo:bar:baz:classifier")]
			options.stubIdsToPortMapping == [(new StubConfiguration("a:b:c")): 3]
			options.username == "foo"
			options.password == "bar"
			options.proxyOptions.proxyHost == "host"
			options.proxyOptions.proxyPort == 4
			options.stubsPerConsumer == true
			options.consumerName == "consumer"
			options.mappingsOutputFolder == "folder"
			options.deleteStubsAfterTest == false
			options.generateStubs == true
			options.failOnNoStubs == false
			options.properties == [foo: "bar"]
			options.httpServerStubConfigurer == Foo
			options.serverId == "server"
	}

	def shouldNotPrintUsernameAndPassword() {
		given:
			StubRunnerOptionsBuilder builder = builder.withOptions(new StubRunnerOptions(1, 2, new FileSystemResource("root"),
					StubRunnerProperties.StubsMode.CLASSPATH, "classifier",
					[new StubConfiguration("a:b:c")], [(new StubConfiguration("a:b:c")): 3], "username123", "password123",
					new StubRunnerOptions.StubRunnerProxyOptions("host", 4), true, "consumer", "folder", false, true, true, [:], Foo, "server"))
			builder.withStubs("foo:bar:baz")
		when:
			String options = builder.build().toString()
		then:
			!options.contains("username123")
			!options.contains("password123")
			options.contains("****")
	}

	@Issue("#462")
	@RestoreSystemProperties
	def shouldSetAllPropsFromSystemProps() {
		given:
			System.setProperty("stubrunner.port.range.min", "1")
			System.setProperty("stubrunner.port.range.max", "2")
			System.setProperty("stubrunner.repository.root", "root")
			System.setProperty("stubrunner.stubs-mode", "LOCAL")
			System.setProperty("stubrunner.classifier", "classifier")
			System.setProperty("stubrunner.ids", "a:b:c,foo:bar:baz:classifier")
			System.setProperty("stubrunner.username", "foo")
			System.setProperty("stubrunner.password", "bar")
			System.setProperty("stubrunner.stubs-per-consumer", "true")
			System.setProperty("stubrunner.consumer-name", "consumer")
			System.setProperty("stubrunner.proxy.host", "host")
			System.setProperty("stubrunner.proxy.port", "4")
			System.setProperty("stubrunner.mappings-output-folder", "folder")
			System.setProperty("stubrunner.properties.foo-bar", "bar")
			System.setProperty("stubrunner.properties.foo-baz", "baz")
			System.setProperty("stubrunner.properties.bar.bar", "foo")
			System.setProperty("stubrunner.delete-stubs-after-test", "false")
			System.setProperty("stubrunner.generate-stubs", "true")
			System.setProperty("stubrunner.fail-on-no-stubs", "false")
			System.setProperty("stubrunner.http-server-stub-configurer", "org.springframework.cloud.contract.stubrunner.Foo")
		when:
			StubRunnerOptions options = StubRunnerOptions.fromSystemProps()
		then:
			options.minPortValue == 1
			options.maxPortValue == 2
			options.stubRepositoryRoot == new ClassPathResource("root")
			options.stubsMode == StubRunnerProperties.StubsMode.LOCAL
			options.stubsClassifier == "classifier"
			options.dependencies == [new StubConfiguration("a:b:c"), new StubConfiguration("foo:bar:baz:classifier")]
			options.username == "foo"
			options.password == "bar"
			options.proxyOptions.proxyHost == "host"
			options.proxyOptions.proxyPort == 4
			options.stubsPerConsumer == true
			options.deleteStubsAfterTest == false
			options.generateStubs == true
			options.failOnNoStubs == false
			options.consumerName == "consumer"
			options.mappingsOutputFolder == "folder"
			options.properties == ["foo-bar": "bar", "foo-baz": "baz", "bar.bar": "foo"]
			options.httpServerStubConfigurer == Foo
	}
}

class Foo implements HttpServerStubConfigurer {

	@Override
	boolean isAccepted(Object httpStubConfiguration) {
		return true
	}
}
