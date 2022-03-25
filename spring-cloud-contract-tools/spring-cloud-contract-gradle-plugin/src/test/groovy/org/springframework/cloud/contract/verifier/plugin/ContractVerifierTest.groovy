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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.springframework.cloud.contract.verifier.config.TestFramework

class ContractVerifierTest {
	DefaultProject project

	@BeforeEach
	void setup() {
		String dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
		File testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
		testFolder.mkdirs()
		project = (DefaultProject) ProjectBuilder.builder().withProjectDir(testFolder).build()
		project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
	}

	@Test
	void "should apply java plugin"() {
		expect:
			project.plugins.hasPlugin(JavaPlugin)
	}

	@Test
	void "should create contracts extension"() {
		expect:
			assert project.extensions.findByType(ContractVerifierExtension) != null
	}

	@Test
	void "should create a test sourceset with java sources"() {
		given:
			ContractVerifierExtension extension = project.extensions.getByType(ContractVerifierExtension)
			Directory projectDir = project.layout.projectDirectory
			SourceSet contractTest = project.convention.getPlugin(JavaPluginConvention).getSourceSets().getByName("contractTest")

		expect:
			assert contractTest != null
			assert contractTest.java.srcDirs.contains(projectDir.dir("src/contractTest/java").asFile)
			assert contractTest.java.srcDirs.contains(extension.generatedTestJavaSourcesDir.get().asFile)
			assert contractTest.resources.srcDirs.contains(projectDir.dir("src/contractTest/resources").asFile)
			assert contractTest.resources.srcDirs.contains(extension.generatedTestResourcesDir.get().asFile)
	}

	@Test
	void "should create a test sourceset with groovy sources, if the groovy plugin is present"() {
		given:
			project.plugins.apply(GroovyPlugin)
			ContractVerifierExtension extension = project.extensions.getByType(ContractVerifierExtension)
			Directory projectDir = project.layout.projectDirectory
			SourceSet contractTest = project.convention.getPlugin(JavaPluginConvention).getSourceSets().getByName("contractTest")

		expect:
			assert contractTest != null
			assert contractTest.java.srcDirs.contains(projectDir.dir("src/contractTest/java").asFile)
			assert contractTest.java.srcDirs.contains(extension.generatedTestJavaSourcesDir.get().asFile)
			assert contractTest.groovy.srcDirs.contains(projectDir.dir('src/contractTest/groovy').asFile)
			assert contractTest.groovy.srcDirs.contains(extension.generatedTestGroovySourcesDir.get().asFile)
			assert contractTest.resources.srcDirs.contains(projectDir.dir("src/contractTest/resources").asFile)
			assert contractTest.resources.srcDirs.contains(extension.generatedTestResourcesDir.get().asFile)
	}

	@Test
	void "should setup dependency configurations"() {
		given:
			Configuration contractTestCompileOnly = project.configurations.contractTestCompileOnly
			Configuration contractTestImplementation = project.configurations.contractTestImplementation
			Configuration contractTestRuntimeOnly = project.configurations.contractTestRuntimeOnly

		expect:
			assert contractTestCompileOnly != null
			assert contractTestCompileOnly.extendsFrom.contains(project.configurations.testCompileOnly)
			assert contractTestImplementation != null
			assert contractTestImplementation.extendsFrom.contains(project.configurations.testImplementation)
			assert contractTestRuntimeOnly != null
			assert contractTestRuntimeOnly.extendsFrom.contains(project.configurations.testRuntimeOnly)
	}

	@Test
	void "should create contract test task"() {
		expect:
			assert project.tasks.named("contractTest").get() != null
	}

	@Test
	void "should create generateContractTests task"() {
		expect:
			assert project.tasks.named("generateContractTests").get() != null
	}

	@Test
	void "should configure generateContractTests task as a dependency of the compileContractTestJava task"() {
		expect:
			assert project.tasks.compileContractTestJava.getDependsOn().contains(project.tasks.named("generateContractTests"))
			assert project.tasks.findByName("compileContractTestGroovy") == null
	}

	@Test
	void "should configure generateContractTests task as a dependency of the compileContractTestGroovy task"() {
		given:
			project.plugins.apply(GroovyPlugin)

		expect:
			assert project.tasks.compileContractTestJava.getDependsOn().contains(project.tasks.named("generateContractTests"))
			assert project.tasks.compileContractTestGroovy.getDependsOn().contains(project.tasks.named("generateContractTests"))
	}

	@Test
	void "should configure generatedTestSourcesDir with the appropriate directories"() {
		when:
			ContractVerifierExtension extension = project.extensions.findByType(ContractVerifierExtension)
			GenerateServerTestsTask generateServerTestsTask = project.tasks.getByName("generateContractTests") as GenerateServerTestsTask

		then:
			assert generateServerTestsTask.generatedTestSourcesDir.get().asFile == extension.generatedTestJavaSourcesDir.get().asFile

		and:
			extension.testFramework.set(TestFramework.SPOCK)

		then:
			assert generateServerTestsTask.generatedTestSourcesDir.get().asFile == extension.generatedTestGroovySourcesDir.get().asFile

		and:
			extension.generatedTestSourcesDir.set(project.file("src/random"))

		then:
			assert generateServerTestsTask.generatedTestSourcesDir.get().asFile == extension.generatedTestSourcesDir.get().asFile
	}

	@Test
	void "should create generateClientStubs task"() {
		expect:
			assert project.tasks.named("generateClientStubs").get() != null
	}

	@Test
	void "should create verifierStubsJar task"() {
		expect:
			assert project.tasks.named("verifierStubsJar").get() != null
	}

	@Test
	void "should configure generateClientStubs task as a dependency of the verifierStubsJar task"() {
		expect:
			project.tasks.verifierStubsJar.getDependsOn().contains(project.tasks.named("generateClientStubs"))
	}

	@Test
	void "should configure generateClientStubs task as a dependency of the publishStubsToScm task"() {
		expect:
			assert project.tasks.publishStubsToScm.getDependsOn().contains(project.tasks.named("generateClientStubs"))
	}

	@Test
	void "should create copyContracts task"() {
		expect:
			assert project.tasks.named("copyContracts").get() != null
	}

	@Test
	void "should configure copyContracts task as a dependency of the verifierStubsJar task"() {
		expect:
			assert project.tasks.verifierStubsJar.getDependsOn().contains(project.tasks.named("generateClientStubs"))
	}

	/**
	 * project.evaluate() is used here in order to trigger the evaluation lifecycle of a project.
	 * This method is currently exposed via the internal API and is subject to change, however, Gradle
	 * does not yet expose a way to test this portion of the lifecycle.
	 *
	 * In the next version, this test will be completely removed as publication will be fully a user
	 * responsibility.
	 */
	@Deprecated
	@Test
	void "should configure maven-publish plugin, if enabled"() {
		given:
			project.plugins.apply(MavenPublishPlugin)
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
			ContractVerifierExtension extension = project.getExtensions().findByType(ContractVerifierExtension)
			extension.with {
				disableStubPublication = false
			}
			project.evaluate() // Currently internal method to trigger afterEvaluate blocks.

		expect:
			PublicationContainer publications = project.extensions.getByType(PublishingExtension).publications
			assert publications.size() > 0
			assert publications.named("stubs") != null
	}

	@Test
	void "should compile"() {
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
			assert extension
	}

	@Test
	void "should property merge scm repository settings for publishing stubs to scm"() {
		given:
			project.plugins.apply(SpringCloudContractVerifierGradlePlugin)
			ContractVerifierExtension extension = project.extensions.findByType(ContractVerifierExtension)
			PublishStubsToScmTask task = project.tasks.findByName(PublishStubsToScmTask.TASK_NAME)

		when:
			extension.contractRepository.with {
				repositoryUrl = "https://git.example.com"
				username = "username"
				password = "password"
				proxyHost = "host"
				proxyPort = 8080
			}

		then:
			assert task.contractRepository.repositoryUrl.get() == "https://git.example.com"
			assert task.contractRepository.username.get() == "username"
			assert task.contractRepository.password.get() == "password"
			assert task.contractRepository.proxyHost.get() == "host"
			assert task.contractRepository.proxyPort.get() == 8080

		and:
			extension.publishStubsToScm.contractRepository.with {
				repositoryUrl = "https://git2.example.com"
				username = "username2"
				password = "password2"
				proxyHost = "host2"
				proxyPort = 8081
			}

		then:
			assert task.contractRepository.repositoryUrl.get() == "https://git2.example.com"
			assert task.contractRepository.username.get() == "username2"
			assert task.contractRepository.password.get() == "password2"
			assert task.contractRepository.proxyHost.get() == "host2"
			assert task.contractRepository.proxyPort.get() == 8081
	}
}
