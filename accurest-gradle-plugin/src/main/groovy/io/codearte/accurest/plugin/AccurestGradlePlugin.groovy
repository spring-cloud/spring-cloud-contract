package io.codearte.accurest.plugin

import io.coderate.accurest.config.AccurestConfigProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author Jakub Kubrynski
 */
class AccurestGradlePlugin implements Plugin<Project> {

	private static final String GENERATE_SERVER_TESTS_TASK_NAME = 'generateAccurest'
	private static final String DSL_TO_WIREMOCK_CLIENT_TASK_NAME = 'generateWiremockClientStubs'

	private static final Class IDEA_PLUGIN_CLASS = org.gradle.plugins.ide.idea.IdeaPlugin
	private static final String GROUP_NAME = "Verification"

	private Project project

	@Override
	void apply(Project project) {
		this.project = project
		AccurestConfigProperties extension = project.extensions.create('accurest', AccurestConfigProperties)

		project.compileTestGroovy.dependsOn(GENERATE_SERVER_TESTS_TASK_NAME)

		setConfigurationDefaults(extension)
		createGenerateTestsTask(extension)
		createAndConfigureGenerateWiremockClientStubsFromDslTask(extension)

		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += extension.generatedTestSourcesDir
					}
				}
			}
		}
	}

	void setConfigurationDefaults(AccurestConfigProperties extension) {
		extension.with {
			generatedTestSourcesDir = project.file("${project.buildDir}/generated-sources/accurest")
			contractsDslDir = project.file("${project.rootDir}/src/test/resources/stubs") //TODO: Use sourceset
			basePackageForTests = 'io.codearte.accurest.tests'
		}
	}

	private void createGenerateTestsTask(AccurestConfigProperties extension) {
		Task task = project.tasks.create(GENERATE_SERVER_TESTS_TASK_NAME, GenerateServerTestsTask)
		task.description = "Generate server tests from GroovyDSL"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			contractsDslDir = { extension.contractsDslDir }
			generatedTestSourcesDir = { extension.generatedTestSourcesDir }
			configProperties = { extension }
		}
	}

	private void createAndConfigureGenerateWiremockClientStubsFromDslTask(AccurestConfigProperties extension) {
		Task task = project.tasks.create(DSL_TO_WIREMOCK_CLIENT_TASK_NAME, GenerateWiremockClientStubsFromDslTask)
		task.description = "Generate Wiremock client stubs from GroovyDSL"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			contractsDslDir = { extension.contractsDslDir }
			stubsOutputDir = { extension.stubsOutputDir }
		}
	}
}
