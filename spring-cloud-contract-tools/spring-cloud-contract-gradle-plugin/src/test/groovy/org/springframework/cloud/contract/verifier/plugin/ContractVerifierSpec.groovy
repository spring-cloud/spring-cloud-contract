package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ContractVerifierSpec extends Specification {
	DefaultProject project

	def setup() {
		String dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
		File testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
		testFolder.mkdirs()
		project = (DefaultProject) ProjectBuilder.builder().withProjectDir(testFolder).build()
		project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
	}

	def "should apply groovy plugin"() {
		expect:
			project.plugins.hasPlugin(GroovyPlugin)
	}

	def "should create contracts extension"() {
		expect:
			project.extensions.findByType(ContractVerifierExtension) != null
	}

	def "should create generateContractTests task"() {
		expect:
			project.tasks.named("generateContractTests") != null
	}

	def "should configure generateContractTests task as a dependency of the check task"() {
		expect:
			project.tasks.check.getDependsOn().contains(project.tasks.named("generateContractTests"))
	}

	def "should create generateClientStubs task"() {
		expect:
			project.tasks.named("generateClientStubs") != null
	}

	def "should create verifierStubsJar task"() {
		expect:
			project.tasks.named("verifierStubsJar") != null
	}

	def "should configure generateClientStubs task as a dependency of the verifierStubsJar task"() {
		expect:
			project.tasks.verifierStubsJar.getDependsOn().contains(project.tasks.named("generateClientStubs"))
	}

	def "should configure generateClientStubs task as a dependency of the publishStubsToScm task"() {
		expect:
			project.tasks.publishStubsToScm.getDependsOn().contains(project.tasks.named("generateClientStubs"))
	}

	def "should create copyContracts task"() {
		expect:
			project.tasks.named("copyContracts") != null
	}

	def "should configure copyContracts task as a dependency of the verifierStubsJar task"() {
		expect:
			project.tasks.verifierStubsJar.getDependsOn().contains(project.tasks.named("copyContracts"))
	}

	def "should create initContracts task"() {
		expect:
			project.tasks.named("initContracts") != null
	}

	def "should configure initContracts task as a dependency of the copyContracts task"() {
		expect:
			project.tasks.copyContracts.getDependsOn().contains(project.tasks.named("initContracts"))
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
			PublicationContainer publications = project.extensions.getByType(PublishingExtension).publications
			publications.size() > 0
			publications.named("stubs") != null
	}

	def "should compile"() {
		given:
			ObjectFactory objectFactory = Stub(ObjectFactory)
			ContractVerifierExtension extension = new ContractVerifierExtension(objectFactory)
			extension.with {

				// tag::package_with_base_classes[]
				packageWithBaseClasses.set('com.example.base')
				// end::package_with_base_classes[]

				// tag::base_class_mappings[]
				baseClassForTests.set("com.example.FooBase")
				baseClassMappings {
					baseClassMapping('.*/com/.*', 'com.example.ComBase')
					baseClassMapping('.*/bar/.*': 'com.example.BarBase')
				}
				// end::base_class_mappings[]
			}
		expect:
			extension
	}
}
