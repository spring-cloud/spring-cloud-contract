/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.springframework.cloud.contract.stubrunner.ScmStubDownloaderBuilder

/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 *     <li>generate tests</li>
 *     <li>generate stubs</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final String GENERATE_SERVER_TESTS_TASK_NAME = 'generateContractTests'
	private static final String DSL_TO_CLIENT_TASK_NAME = 'generateClientStubs'
	@PackageScope static final String COPY_CONTRACTS_TASK_NAME = 'copyContracts'
	private static final String VERIFIER_STUBS_JAR_TASK_NAME = 'verifierStubsJar'
	private static final String PUBLISH_STUBS_TO_SCM_TASK_NAME = 'publishStubsToScm'

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
		createAndConfigureMavenPublishPlugin(stubsJar, extension)
		createGenerateTestsTask(extension, copyContracts, downloader)
		createAndConfigureGenerateClientStubs(extension, copyContracts)
		createAndConfigurePublishStubsToScmTask(extension, downloader)
		addIdeaTestSources(project, extension)
	}

	private addIdeaTestSources(Project project, ContractVerifierExtension extension) {
		def hasIdea = new File(project.rootDir, ".idea").exists()
		if (hasIdea) {
			project.apply(plugin: 'idea')
			project.idea {
				module {
					testSourceDirs += extension.generatedTestSourcesDir
					testSourceDirs += extension.generatedTestResourcesDir
					testSourceDirs += extension.contractsDslDir
				}
			}
		}
	}

	private void setConfigurationDefaults(ContractVerifierExtension extension) {
		extension.with {
			generatedTestSourcesDir = generatedTestSourcesDir ?: project.file("${project.buildDir}/generated-test-sources/contracts")
			generatedTestSourcesDir.mkdirs()
			generatedTestResourcesDir = generatedTestResourcesDir ?: project.file("${project.buildDir}/generated-test-resources/contracts")
			generatedTestResourcesDir.mkdirs()
			contractsDslDir = contractsDslDir ?: defaultContractsDir() //TODO: Use sourceset
			contractsDslDir.mkdirs()
			stubsOutputDir = stubsOutputDir ?: project.file("${project.buildDir}/stubs/")
			stubsOutputDir.mkdirs()
		}
	}

	private File defaultContractsDir() {
		return project.file("${project.projectDir}/src/test/resources/contracts")
	}

	private void createGenerateTestsTask(ContractVerifierExtension extension, Task copyContracts,
										 GradleContractsDownloader gradleContractsDownloader) {
		Task task = project.tasks.create(GENERATE_SERVER_TESTS_TASK_NAME, GenerateServerTestsTask)
		task.description = "Generate server tests from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			generatedTestSourcesDir = { extension.generatedTestSourcesDir }
			configProperties = { extension }
		}
		task.enabled = !project.gradle.startParameter.excludedTaskNames.contains("test")
		task.dependsOn copyContracts
		project.tasks.findByName("compileTestJava").dependsOn(task)
	}

	private void createAndConfigurePublishStubsToScmTask(ContractVerifierExtension extension,
														 GradleContractsDownloader gradleContractsDownloader) {
		Task task = project.tasks.create(PUBLISH_STUBS_TO_SCM_TASK_NAME, PublishStubsToScmTask)
		task.description = "The generated stubs get committed to the SCM repo and pushed to origin"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			configProperties = { extension }
			stubsOutputDir = { extension.stubsOutputDir }
		}
		task.onlyIf {
			String contractRepoUrl = extension.contractRepository.repositoryUrl ?: ""
			if (!contractRepoUrl || !ScmStubDownloaderBuilder.isProtocolAccepted(contractRepoUrl)) {
				project.logger.info("Skipping pushing stubs to scm since your contracts repository URL doesn't match any of the accepted protocols")
				return false
			}
			return true
		}
		task.dependsOn DSL_TO_CLIENT_TASK_NAME
	}

	private Task createAndConfigureGenerateClientStubs(ContractVerifierExtension extension,
													   Task copyContracts) {
		Task task = project.tasks.create(DSL_TO_CLIENT_TASK_NAME, GenerateClientStubsFromDslTask)
		task.description = "Generate client stubs from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			downloader = { gradleContractsDownloader }
			stubsOutputDir = { extension.stubsOutputDir }
			configProperties = { extension }
		}
		task.dependsOn copyContracts
		return task
	}

	private Task createAndConfigureStubsJarTasks(ContractVerifierExtension extension) {
		Task task = stubsTask()
		if (task) {
			project.logger.info("Spring Cloud Contract Verifier Plugin: Stubs jar task was present - won't create one. Remember about adding it to artifacts as an archive!")
			return task
		} else {
			task = project.tasks.create(type: Jar, name: VERIFIER_STUBS_JAR_TASK_NAME,
					dependsOn: DSL_TO_CLIENT_TASK_NAME) {
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

	private void createAndConfigureMavenPublishPlugin(Task stubsTask, ContractVerifierExtension extension) {
		if (!classIsOnClasspath("org.gradle.api.publish.maven.plugins.MavenPublishPlugin")) {
			project.logger.debug("Maven Publish Plugin is not present - won't add default publication")
			return
		}
		project.logger.debug("Spring Cloud Contract Verifier Plugin: Generating default publication")
		project.afterEvaluate {
			if (extension.isDisableStubPublication()) {
				project.logger.info("You've switched off the stub publication - won't add default publication")
				return
			}
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
