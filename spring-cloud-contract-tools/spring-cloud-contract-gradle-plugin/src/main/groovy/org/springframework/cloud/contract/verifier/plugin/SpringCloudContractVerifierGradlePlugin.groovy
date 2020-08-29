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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.springframework.cloud.contract.verifier.config.TestFramework
/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 *     <li>generate tests</li>
 *     <li>generate stubs</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 *
 * @since 1.0.0
 */
@CompileStatic
class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final String VERIFIER_STUBS_JAR_TASK_NAME = 'verifierStubsJar'
	private static final String GROUP_NAME = "Verification"
	private static final String EXTENSION_NAME = 'contracts'

	private Project project

	@Override
	void apply(Project project) {
		this.project = project
		project.plugins.apply(GroovyPlugin)
		ContractVerifierExtension extension = project.extensions.create(EXTENSION_NAME, ContractVerifierExtension)

		TaskProvider<ContractsCopyTask> copyContracts = createAndConfigureCopyContractsTask(extension)
		TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs = createAndConfigureGenerateClientStubs(extension, copyContracts)

		createAndConfigureStubsJarTasks(extension, copyContracts, generateClientStubs)
		createGenerateTestsTask(extension, copyContracts)
		createAndConfigurePublishStubsToScmTask(extension, generateClientStubs)
		project.afterEvaluate {
			addIdeaTestSources(project, extension)
			applyDefaultSourceSets(extension)
		}
	}

	// This must be called within afterEvaluate due to getting data from extension, which must be initialised first:
	@CompileDynamic
	private void applyDefaultSourceSets(ContractVerifierExtension extension) {
		boolean sourceSetPresent = extension.getSourceSet().isPresent()
		String sourceSet = sourceSet(sourceSetPresent, extension)
		String sourceSetType = extension.testFramework.get() == TestFramework.SPOCK ? "groovy" : "java"
		project.sourceSets."${sourceSet}"."${sourceSetType}" {
			project.logger.
					info("Registering ${extension.generatedTestSourcesDir.get().asFile} as test source directory")
			srcDir extension.generatedTestSourcesDir.get().asFile
		}
		project.sourceSets.test.resources {
			project.logger.
					info("Registering ${extension.generatedTestResourcesDir.get().asFile} as test resource directory")
			srcDir extension.generatedTestResourcesDir.get().asFile
		}
	}

	private String sourceSet(boolean sourceSetPresent, ContractVerifierExtension extension) {
		if (sourceSetPresent) {
			return extension.getSourceSet().get()
		}
		return "test"
	}

	// This must be called within afterEvaluate due to getting data from extension, which must be initialised first:
	@CompileDynamic
	private addIdeaTestSources(Project project, ContractVerifierExtension extension) {
		boolean hasIdea = new File(project.rootDir, ".idea").exists()
		if (hasIdea) {
			project.apply(plugin: 'idea')
			project.idea {
				module {
					testSourceDirs += extension.generatedTestSourcesDir.get().asFile
					testSourceDirs += extension.generatedTestResourcesDir.get().asFile
					testSourceDirs += extension.contractsDslDir.get().asFile
				}
			}
		}
	}

	private void createGenerateTestsTask(ContractVerifierExtension extension, TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateServerTestsTask> task = project.tasks.register(GenerateServerTestsTask.TASK_NAME, GenerateServerTestsTask)
		task.configure {
			it.description = "Generate server tests from the contracts"
			it.group = GROUP_NAME
			it.enabled = !project.gradle.startParameter.excludedTaskNames.contains("test")
			it.config = GenerateServerTestsTask.fromExtension(extension, copyContracts)

			it.dependsOn copyContracts
		}
		project.tasks.findByName("compileTestJava").dependsOn(task)
		project.tasks.findByName("check").dependsOn(task)
	}

	private void createAndConfigurePublishStubsToScmTask(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<PublishStubsToScmTask> task = project.tasks.register(PublishStubsToScmTask.TASK_NAME, PublishStubsToScmTask)
		task.configure {
			it.description = "The generated stubs get committed to the SCM repo and pushed to origin"
			it.group = GROUP_NAME
			it.config = PublishStubsToScmTask.fromExtension(extension, project.objects)

			it.dependsOn generateClientStubs
		}
	}

	private TaskProvider<GenerateClientStubsFromDslTask> createAndConfigureGenerateClientStubs(ContractVerifierExtension extension,
			TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateClientStubsFromDslTask> task = project.tasks.register(GenerateClientStubsFromDslTask.TASK_NAME, GenerateClientStubsFromDslTask, { generateClientStubs ->
			generateClientStubs.group = GROUP_NAME
			generateClientStubs.description = "Generate client stubs from the contracts"

			generateClientStubs.contractsDslDir.convention(copyContracts.flatMap { it.copiedContractsFolder })
			generateClientStubs.excludedFiles.convention(extension.excludedFiles)
			generateClientStubs.excludeBuildFolders.convention(extension.excludeBuildFolders)

			generateClientStubs.stubsOutputDir.convention(extension.stubsOutputDir.dir(buildRootPath() + File.separator + GenerateClientStubsFromDslTask.DEFAULT_MAPPINGS_FOLDER))

			generateClientStubs.dependsOn(copyContracts)
		})
		return task
	}

	private TaskProvider<Task> createAndConfigureStubsJarTasks(ContractVerifierExtension extension,
			TaskProvider<ContractsCopyTask> copyContracts,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Task> task = stubsTask()
		if (task) {
			// How is this possible? Where can it come from?
			project.logger.info("Spring Cloud Contract Verifier Plugin: Stubs jar task was present - won't create one. Remember about adding it to artifacts as an archive!")
		}
		else {
			task = createStubsJarTask(extension, generateClientStubs)
		}
		task.configure {
			it.dependsOn copyContracts
		}
		createAndConfigureMavenPublishPlugin(task, extension)
		return task
	}

	@Deprecated
	private void createAndConfigureMavenPublishPlugin(TaskProvider<Task> stubsTask, ContractVerifierExtension extension) {
		if (!classIsOnClasspath("org.gradle.api.publish.maven.plugins.MavenPublishPlugin")) {
			project.logger.debug("Maven Publish Plugin is not present - won't add default publication")
			return
		}
		// This must be called within afterEvaluate due to getting data from extension, which must be initialised first:
		project.afterEvaluate {
			project.logger.debug("Spring Cloud Contract Verifier Plugin: Generating default publication")
			if (extension.disableStubPublication.get()) {
				project.logger.info("You've switched off the stub publication - won't add default publication")
				return
			}
			project.plugins.withType(MavenPublishPlugin) { def publishingPlugin ->
				def publishingExtension = project.extensions.findByName('publishing')
				if (hasStubsPublication(publishingExtension)) {
					project.logger.info("Spring Cloud Contract Verifier Plugin: Stubs publication was present - won't create a new one. Remember about passing stubs as artifact")
				}
				else {
					project.logger.debug("Spring Cloud Contract Verifier Plugin: Stubs publication is not present - will create one")
					setPublications(publishingExtension, stubsTask)
				}
			}
		}
	}

	@CompileDynamic
	@Deprecated
	private void setPublications(def publishingExtension, TaskProvider<Task> stubsTask) {
		project.logger.warn("Spring Cloud Contract Verifier Plugin: Creating stubs publication is deprecated")
		publishingExtension.publications {
			stubs(MavenPublication) {
				artifactId "${project.name}"
				artifact stubsTask.get() // TODO: How to make it lazily initialised?
			}
		}
	}

	private TaskProvider<Task> stubsTask() {
		try {
			return project.tasks.named(VERIFIER_STUBS_JAR_TASK_NAME)
		}
		catch (Exception e) {
			return null
		}
	}

	@CompileDynamic
	@Deprecated
	private boolean hasStubsPublication(def publishingExtension) {
		try {
			return publishingExtension.publications.getByName('stubs')
		}
		catch (Exception e) {
			return false
		}
	}

	// TODO: Can we define inputs / outputs and make it incremental?
	@CompileDynamic
	private TaskProvider<Task> createStubsJarTask(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Jar> task = project.tasks.register(VERIFIER_STUBS_JAR_TASK_NAME, Jar)
		task.configure {
			it.description = "Creates the stubs JAR task"
			it.group = GROUP_NAME
			it.getArchiveBaseName().set(project.name)
			it.getArchiveClassifier().set(extension.stubsSuffix)
			it.from { extension.stubsOutputDir }

			it.dependsOn generateClientStubs
		}
		project.artifacts {
			archives task
		}
		return task
	}

	private TaskProvider<ContractsCopyTask> createAndConfigureCopyContractsTask(ContractVerifierExtension extension) {
		TaskProvider<ContractsCopyTask> task = project.tasks.register(ContractsCopyTask.TASK_NAME, ContractsCopyTask, { contractsCopyTask ->
			contractsCopyTask.group = GROUP_NAME
			contractsCopyTask.description = "Copies contracts to the output folder"

			contractsCopyTask.convertToYaml.convention(extension.convertToYaml)
			contractsCopyTask.failOnNoContracts.convention(extension.failOnNoContracts)
			contractsCopyTask.contractsDirectory.convention(extension.contractsDslDir)
			contractsCopyTask.contractDependency.with {
				groupId.convention(extension.contractDependency.groupId)
				artifactId.convention(extension.contractDependency.artifactId)
				version.convention(extension.contractDependency.version)
				classifier.convention(extension.contractDependency.classifier)
				stringNotation.convention(extension.contractDependency.stringNotation)
			}
			contractsCopyTask.contractRepository.with {
				repositoryUrl.convention(extension.contractRepository.repositoryUrl)
				username.convention(extension.contractRepository.username)
				password.convention(extension.contractRepository.password)
				proxyHost.convention(extension.contractRepository.proxyHost)
				proxyPort.convention(extension.contractRepository.proxyPort)
			}
			contractsCopyTask.contractsMode.convention(extension.contractsMode)
			contractsCopyTask.excludeBuildFolders.convention(extension.excludeBuildFolders)
			contractsCopyTask.deleteStubsAfterTest.convention(extension.deleteStubsAfterTest)

			contractsCopyTask.copiedContractsFolder.convention(extension.stubsOutputDir.dir(buildRootPath() + File.separator + ContractsCopyTask.CONTRACTS))
			contractsCopyTask.backupContractsFolder.convention(extension.stubsOutputDir.dir(buildRootPath() + File.separator + ContractsCopyTask.BACKUP))
		})
		return task
	}

	@Deprecated
	private boolean classIsOnClasspath(String className) {
		try {
			Class.forName(className)
			return true
		}
		catch (Exception e) {
			project.logger.debug("Maven Publish Plugin is not available")
		}
		return false
	}

	private String buildRootPath() {
		String groupId = project.group as String
		String artifactId = project.name
		String version = project.version
		return "META-INF/${groupId}/${artifactId}/${version}"
	}
}
