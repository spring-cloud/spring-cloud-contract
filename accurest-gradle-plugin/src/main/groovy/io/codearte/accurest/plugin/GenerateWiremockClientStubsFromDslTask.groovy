package io.codearte.accurest.plugin

import io.codearte.accurest.wiremock.DslToWiremockClientConverter
import io.codearte.accurest.wiremock.RecursiveFilesConverter
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
class GenerateWiremockClientStubsFromDslTask extends ConventionTask {

	@InputDirectory
	File contractsDslDir
	@InputDirectory
	File stubsOutputDir

	@TaskAction
	void generate() {
		project.logger.info("Accurest Plugin: Invoking GroovyDSL to Wiremock client stubs conversion")
		project.logger.debug("From '${getContractsDslDir()}' to '${getStubsOutputDir()}'")

		RecursiveFilesConverter converter = new RecursiveFilesConverter(new DslToWiremockClientConverter(), getContractsDslDir(),
				getStubsOutputDir())
		converter.processFiles()
	}
}
