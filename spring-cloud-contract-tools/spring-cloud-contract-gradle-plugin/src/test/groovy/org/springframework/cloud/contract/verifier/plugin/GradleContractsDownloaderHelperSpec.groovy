package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.internal.provider.DefaultPropertyState
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.springframework.cloud.contract.stubrunner.StubConfiguration
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 */
class GradleContractsDownloaderHelperSpec extends Specification {

	ObjectFactory objectFactory = Mock(ObjectFactory)

	def setup() {
		// Is there any better way to say that I need a new object on each interaction with mock?
		objectFactory.property(String) >>> [prop(String), prop(String), prop(String), prop(String), prop(String)]
	}

	def "should parse dependency via string notation"() {
		given:
			String stringNotation = "com.example:foo:1.0.0:stubs"
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.stringNotation.set(stringNotation)
		when:
			StubConfiguration stubConfig = GradleContractsDownloaderHelper.stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via direct setting"() {
		given:
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.groupId.set("com.example")
			dep.artifactId.set("foo")
			dep.version.set("1.0.0")
			dep.classifier.set("stubs")
		when:
			StubConfiguration stubConfig = GradleContractsDownloaderHelper.stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via string notation with methods"() {
		given:
			String stringNotation = "com.example:foo:1.0.0:stubs"
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.stringNotation.set(stringNotation)
		when:
			StubConfiguration stubConfig = GradleContractsDownloaderHelper.stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	def "should parse dependency via direct setting with methods"() {
		given:
			ContractVerifierExtension.Dependency dep = new ContractVerifierExtension.Dependency(objectFactory)
			dep.groupId.set("com.example")
			dep.artifactId.set("foo")
			dep.version.set("1.0.0")
			dep.classifier.set("stubs")
		when:
			StubConfiguration stubConfig = GradleContractsDownloaderHelper.stubConfiguration(dep)
		then:
			stubConfig.groupId == "com.example"
			stubConfig.artifactId == "foo"
			stubConfig.version == "1.0.0"
			stubConfig.classifier == "stubs"
	}

	// Have to use this internal property impl here. Is there some better way?
	static <T> Property<T> prop(Class<T> aClass) {
		return new DefaultPropertyState(aClass)
	}
}
