package io.codearte.accurest.plugin

import io.coderate.accurest.TestGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Jakub Kubrynski
 */
class AccurestGradlePlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.extensions.create('accurest', AccurestPluginExtension)
		project.task('generateAccurest') << {
			AccurestPluginExtension accurest = project.accurest
			project.logger.info("Accurest Plugin: Invoking test sources generation")

//			project.sourceSets.test.allSource.class.getDeclaredMethods().each {println it.name}

			try {
				TestGenerator generator = new TestGenerator(project.projectDir.path + '/src/test/resources/' + accurest.stubsBaseDirectory, accurest.basePackageForTests,
						accurest.baseClassForTests, accurest.ruleClassForTests,
						accurest.targetFramework, project.buildDir.path + '/' + accurest.generatedTestSourcesDir)
				generator.generate()
			} catch (IllegalStateException e) {
				project.logger.error("Accurest Plugin: {}", e.getMessage())
			}
		}
	}

}
