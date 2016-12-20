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

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 *     <li>generate tests</li>
 *     <li>generate stubs</li>
 * </ul>
 *
 * Also adds the necessary {@code testCompile} dependencies
 *
 * <ul>
 *     <li>WireMock</li>
 *     <li>JSON Assert</li>
 *     <li>AssertJ</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final String GENERATE_SERVER_TESTS_TASK_NAME = 'generateContractTests'
	private static final String DSL_TO_WIREMOCK_CLIENT_TASK_NAME = 'generateWireMockClientStubs'
	@PackageScope static final String COPY_CONTRACTS_TASK_NAME = 'copyContracts'
	private static final String VERIFIER_STUBS_JAR_TASK_NAME = 'verifierStubsJar'

	private static final Class IDEA_PLUGIN_CLASS = org.gradle.plugins.ide.idea.IdeaPlugin
	private static final String GROUP_NAME = "Verification"
	private static final String EXTENSION_NAME = 'contracts'

	private Project project

	@Override
	void apply(Project project) {
		this.project = project
		project.plugins.apply(GroovyPlugin)
		ContractVerifierExtension extension = project.extensions.create(EXTENSION_NAME, ContractVerifierExtension)
		GradleContractsDownloader downloader = new GradleContractsDownloader(this.project, this.project.logger)
		project.check.dependsOn(GENERATE_SERVER_TESTS_TASK_NAME)
		setConfigurationDefaults(extension)
		Task stubsJar = createAndConfigureStubsJarTasks(extension)
		Task copyContracts = createAndConfigureCopyContractsTask(stubsJar, downloader, extension)
		createAndConfigureMavenPublishPlugin(stubsJar)
		createGenerateTestsTask(extension, copyContracts)
		createAndConfigureGenerateWireMockClientStubsFromDslTask(extension, copyContracts)
		addProjectDependencies(project)
		addIdeaTestSources(project, extension)
	}

	private addIdeaTestSources(Project project, extension) {
		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += extension.generatedTestSourcesDir
						testSourceDirs += extension.contractsDslDir
					}
				}
			}
		}
	}

	private void addProjectDependencies(Project project) {
		project.dependencies.add("testCompile", "com.github.tomakehurst:wiremock:2.1.7")
		project.dependencies.add("testCompile", "com.toomuchcoding.jsonassert:jsonassert:0.4.7")
		project.dependencies.add("testCompile", "org.assertj:assertj-core:2.3.0")
	}

	private void setConfigurationDefaults(ContractVerifierExtension extension) {
		extension.with {
			generatedTestSourcesDir = generatedTestSourcesDir ?: project.file("${project.buildDir}/generated-test-sources/contracts")
			contractsDslDir = contractsDslDir ?: defaultContractsDir() //TODO: Use sourceset
			basePackageForTests = basePackageForTests ?: 'org.springframework.cloud.contract.verifier.tests'
			stubsOutputDir = stubsOutputDir ?: project.file("${project.buildDir}/stubs/")
		}
	}

	private File defaultContractsDir() {
		return project.file("${project.projectDir}/src/test/resources/contracts")
	}

	private void createGenerateTestsTask(ContractVerifierExtension extension, Task copyContracts) {
		Task task = project.tasks.create(GENERATE_SERVER_TESTS_TASK_NAME, GenerateServerTestsTask)
		task.description = "Generate server tests from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			generatedTestSourcesDir = { extension.generatedTestSourcesDir }
			configProperties = { extension }
		}
		task.dependsOn copyContracts
	}

	private void createAndConfigureGenerateWireMockClientStubsFromDslTask(ContractVerifierExtension extension,
																		  Task copyContracts) {
		Task task = project.tasks.create(DSL_TO_WIREMOCK_CLIENT_TASK_NAME, GenerateWireMockClientStubsFromDslTask)
		task.description = "Generate WireMock client stubs from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			stubsOutputDir = { extension.stubsOutputDir }
			configProperties = { extension }
		}
		task.dependsOn copyContracts
	}

	private Task createAndConfigureStubsJarTasks(ContractVerifierExtension extension) {
		Task task = stubsTask()
		if (task) {
			project.logger.info("Spring Cloud Contract Verifier Plugin: Stubs jar task was present - won't create one. Remember about adding it to artifacts as an archive!")
			return task
		} else {
			task = project.tasks.create(type: Jar, name: VERIFIER_STUBS_JAR_TASK_NAME,
					dependsOn: DSL_TO_WIREMOCK_CLIENT_TASK_NAME) {
				baseName = project.name
				classifier = extension.stubsSuffix
				from { extension.stubsOutputDir ?: project.file("${project.buildDir}/stubs") }
			}
			task.description = "Creates the stubs JAR task"
			task.group = GROUP_NAME
			project.artifacts {
				archives task
			}
			return task
		}
	}

	private Task stubsTask() {
		try {
			return project.tasks.getByName(VERIFIER_STUBS_JAR_TASK_NAME)
		} catch (Exception e) {
			return null
		}
	}

	private Task createAndConfigureCopyContractsTask(Task stubs,
													GradleContractsDownloader gradleContractsDownloader,
													ContractVerifierExtension contractVerifierExtension) {
		Task task = project.tasks.create(COPY_CONTRACTS_TASK_NAME, ContractsCopyTask)
		task.description = "Copies contracts to the output folder"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			extension = { contractVerifierExtension }
		}
		stubs.dependsOn task
		return task
	}

	private void createAndConfigureMavenPublishPlugin(Task stubsTask) {
		if (!classIsOnClasspath("org.gradle.api.publish.maven.plugins.MavenPublishPlugin")) {
			project.logger.debug("Maven Publish Plugin is not present - won't add default publication")
			return
		}
		project.logger.debug("Spring Cloud Contract Verifier Plugin: Generating default publication")
		project.afterEvaluate {
			project.plugins.withType(MavenPublishPlugin) { def publishingPlugin ->
				def publishingExtension = project.extensions.findByName('publishing')
				if (!hasPublication(publishingExtension)) {
					project.logger.debug("Spring Cloud Contract Verifier Plugin: Stubs publication is not present - will create one")
					publishingExtension.publications {
						stubs(MavenPublication) {
							artifactId "${project.name}"
							artifact stubsTask
						}
					}
				} else {
					project.logger.info("Spring Cloud Contract Verifier Plugin: Stubs publication was present - won't create a new one. Remember about passing stubs as artifact")
				}
			}
		}
	}

	private boolean hasPublication(def publishingExtension) {
		try {
			return publishingExtension.publications.getByName('stubs')
		} catch (Exception e) {
			return false
		}
	}

	private boolean classIsOnClasspath(String className) {
		try {
			Class.forName(className)
			return true
		} catch (Exception e) {
			project.logger.debug("Maven Publish Plugin is not available")
		}
		return false
	}
}
