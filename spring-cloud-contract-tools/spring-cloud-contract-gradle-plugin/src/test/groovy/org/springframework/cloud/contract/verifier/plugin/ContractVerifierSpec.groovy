package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Task
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ContractVerifierSpec extends Specification {
	DefaultProject project

	def setup() {
		def dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
		def testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
		testFolder.mkdirs()
		project = (DefaultProject) ProjectBuilder.builder().withProjectDir(testFolder).build()
	}

	def "should apply groovy plugin"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.plugins.hasPlugin(GroovyPlugin)
	}

	def "should create contracts extension"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.extensions.findByType(ContractVerifierExtension) != null
	}

	def "should create generateContractTests task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.findByName("generateContractTests") != null
	}

	def "should configure generateContractTests task as a dependency of the check task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.check.getDependsOn().contains("generateContractTests")
	}

	def "should create generateWireMockClientStubs task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.findByName("generateWireMockClientStubs") != null
	}

	def "should create verifierStubsJar task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.findByName("verifierStubsJar") != null
	}

	def "should configure generateWireMockClientStubs task as a dependency of the verifierStubsJar task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.verifierStubsJar.getDependsOn().contains("generateWireMockClientStubs")
	}

	def "should create copyContracts task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.tasks.findByName("copyContracts") != null
	}

	def "should configure copyContracts task as a dependency of the verifierStubsJar task"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
			Task copyContracts = project.tasks.copyContracts
			assert copyContracts != null

		expect:
			project.tasks.verifierStubsJar.getDependsOn().contains(copyContracts)
	}

	/**
	 * project.evaluate() is used here in order to trigger the evaluation lifecycle of a project.
	 * This method is currently exposed via the internal API and is subject to change, however, Gradle
	 * does not yet expose a way to test this portion of the lifecycle.
	 */
	def "should configure maven-publish plugin, if available"() {
		given:
			project.plugins.apply(MavenPublishPlugin)
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
			project.evaluate() // Currently internal method to trigger afterEvaluate blocks.

		expect:
			def publications = project.extensions.getByType(PublishingExtension).publications
			publications.size() > 0
			publications.findByName("stubs") != null
	}

	def "should add wiremock as a testCompile dependency"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.configurations.testCompile.dependencies.find {
				it.group == "com.github.tomakehurst" && it.name == "wiremock"
			} != null
	}

	def "should add jsonassert as a testCompile dependency"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.configurations.testCompile.dependencies.find {
				it.group == "com.toomuchcoding.jsonassert" && it.name == "jsonassert"
			} != null
	}

	def "should add assertj-core as a testCompile dependency"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)

		expect:
			project.configurations.testCompile.dependencies.find {
				it.group == "org.assertj" && it.name == "assertj-core"
			} != null
	}

	def "should compile"() {
		given:
			ContractVerifierExtension extension = new ContractVerifierExtension()
			extension.with {

				// tag::package_with_base_classes[]
				packageWithBaseClasses = 'com.example.base'
				// end::package_with_base_classes[]

				// tag::base_class_mappings[]
				baseClassForTests = "com.example.FooBase"
				baseClassMappings {
					baseClassMapping('.*/com/.*', 'com.example.ComBase')
					baseClassMapping('.*/bar/.*':'com.example.BarBase')
				}
				// end::base_class_mappings[]
			}
		expect:
			extension
	}
}
