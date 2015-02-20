package io.codearte.accurest.plugin

import io.coderate.accurest.AccurestException
import io.coderate.accurest.TestGenerator
import io.coderate.accurest.config.AccurestConfigProperties
import org.gradle.api.GradleException
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

	@Override
	void apply(Project project) {
		AccurestConfigProperties extension = project.extensions.create('accurest', AccurestConfigProperties)

		project.compileTestGroovy.dependsOn(GENERATE_SERVER_TESTS_TASK_NAME)

		createGenerateTestsTask(project, extension)
		createAndConfigureGenerateWiremockClientStubsFromDslTask(project, extension)

		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += project.file(buildGeneratedSourcesDir(project, extension))
					}
				}
			}
		}
	}

	private Task createGenerateTestsTask(Project project, AccurestConfigProperties extension) {
		project.task(GENERATE_SERVER_TESTS_TASK_NAME) << {
			project.logger.info("Accurest Plugin: Invoking test sources generation")

			extension.stubsBaseDirectory = project.projectDir.path + File.separator + extension.stubsBaseDirectory

			project.sourceSets.test.groovy {
				project.logger.info("Registering $extension.generatedTestSourcesDir as test source directory")
				srcDir extension.generatedTestSourcesDir
			}

			extension.generatedTestSourcesDir = buildGeneratedSourcesDir(project, extension)

			try {
				TestGenerator generator = new TestGenerator(extension)
				int generatedClasses = generator.generate()
				project.logger.info("Generated {} test classes", generatedClasses)
			} catch (AccurestException e) {
				throw new GradleException("Accurest Plugin exception: ${e.message}", e)
			}
		}
	}

	private void createAndConfigureGenerateWiremockClientStubsFromDslTask(Project project, AccurestConfigProperties extension) {
		Task task = project.tasks.create(DSL_TO_WIREMOCK_CLIENT_TASK_NAME, GenerateWiremockClientStubsFromDslTask)
		task.description = "Generate"
		task.conventionMapping.with {
			groovyDslDir = { extension.groovyDslDir }
			generatedWiremockClientStubsDir = { extension.generatedWiremockClientStubsDir }
		}
	}

	private String buildGeneratedSourcesDir(Project project, def extension) {
		String moduleDir
		if (project.getParent()) {
			moduleDir = project.name + File.separator + extension.generatedTestSourcesDir
		} else {
			moduleDir = extension.generatedTestSourcesDir
		}
		moduleDir
	}
}
