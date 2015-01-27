package io.codearte.accurest.plugin

import io.coderate.accurest.TestGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Jakub Kubrynski
 */
class AccurestGradlePlugin implements Plugin<Project> {

	private static final String TASK_NAME = 'generateAccurest'

	private static final Class IDEA_PLUGIN_CLASS = org.gradle.plugins.ide.idea.IdeaPlugin

	@Override
	void apply(Project project) {
		AccurestPluginExtension extension = project.extensions.create('accurest', AccurestPluginExtension)

		project.compileTestGroovy.dependsOn(TASK_NAME)

		project.task(TASK_NAME) << {
			project.logger.info("Accurest Plugin: Invoking test sources generation")

			project.sourceSets.test.groovy {
				srcDir extension.generatedTestSourcesDir
			}

			try {
				String resourceDirectory = project.projectDir.path + '/src/test/resources/' + extension.stubsBaseDirectory
				TestGenerator generator = new TestGenerator(resourceDirectory,
						extension.basePackageForTests, extension.baseClassForTests, extension.ruleClassForTests,
						extension.targetFramework, extension.testMode,
						extension.generatedTestSourcesDir)
				generator.generate()
			} catch (IllegalStateException e) {
				project.logger.error("Accurest Plugin: {}", e.getMessage())
			}
		}

		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += new File(extension.generatedTestSourcesDir)
					}
				}
			}
		}

	}

}
