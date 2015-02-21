package io.codearte.accurest.plugin

import io.codearte.accurest.wiremock.DslToWiremockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
class GenerateWiremockClientStubsFromDslTask extends DefaultTask {

	@InputDirectory File groovyDslDir
	@InputDirectory File generatedWiremockClientStubsDir

	@TaskAction
	void generate() {
		project.logger.info("Accurest Plugin: Invoking GroovyDSL to Wiremock client stubs conversion")
		project.logger.debug("From '${getGroovyDslDir()}' to '${getGeneratedWiremockClientStubsDir()}'")

		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWiremockClientConverter(), getGroovyDslDir(),
				getGeneratedWiremockClientStubsDir())
		converter.processFiles()
	}
}
