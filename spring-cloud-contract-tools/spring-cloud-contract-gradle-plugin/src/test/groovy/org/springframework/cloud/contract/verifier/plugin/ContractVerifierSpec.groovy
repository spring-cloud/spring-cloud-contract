/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.plugin


import org.gradle.api.internal.project.DefaultProject
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
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
			ContractVerifierExtension extension = project.getExtensions().findByType(ContractVerifierExtension)
			extension.with {

				// tag::package_with_base_classes[]
				packageWithBaseClasses = 'com.example.base'
				// end::package_with_base_classes[]

				// tag::base_class_mappings[]
				baseClassForTests = "com.example.FooBase"
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
