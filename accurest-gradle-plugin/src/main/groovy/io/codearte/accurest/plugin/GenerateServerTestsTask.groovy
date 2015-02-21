package io.codearte.accurest.plugin

import io.coderate.accurest.AccurestException
import io.coderate.accurest.TestGenerator
import io.coderate.accurest.config.AccurestConfigProperties
import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GenerateServerTestsTask extends ConventionTask {

	@InputDirectory
	File contractsDslDir
	@OutputDirectory
	File generatedTestSourcesDir

	//TODO: How to deal with @Input*, @Output* and that domain object?
	AccurestConfigProperties configProperties

	@TaskAction
	void generate() {
		project.logger.info("Accurest Plugin: Invoking test sources generation")

		project.sourceSets.test.groovy {
			project.logger.info("Registering ${getConfigProperties().generatedTestSourcesDir} as test source directory")
			srcDir getConfigProperties().generatedTestSourcesDir
		}

		try {
			//TODO: What with that? How to pass?
			TestGenerator generator = new TestGenerator(getConfigProperties())
			int generatedClasses = generator.generate()
			project.logger.info("Generated {} test classes", generatedClasses)
		} catch (AccurestException e) {
			throw new GradleException("Accurest Plugin exception: ${e.message}", e)
		}
	}
}
