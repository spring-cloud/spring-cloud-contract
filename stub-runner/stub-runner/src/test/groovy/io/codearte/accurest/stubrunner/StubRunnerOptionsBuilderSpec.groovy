package io.codearte.accurest.stubrunner

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
}
