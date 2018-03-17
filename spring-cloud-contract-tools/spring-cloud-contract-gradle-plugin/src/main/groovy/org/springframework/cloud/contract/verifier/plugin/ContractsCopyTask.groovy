package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * Task that copies the contracts in order for the jar task to
 * generate the jar. It takes into consideration the inclusion
 * patterns when working with repo with shared contracts.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.2
 */
@PackageScope
class ContractsCopyTask extends ConventionTask {
	ContractVerifierExtension extension
	GradleContractsDownloader downloader

	@TaskAction
	void copy() {
		ContractVerifierConfigProperties props = ExtensionToProperties.fromExtension(getExtension())
		File file = getDownloader().downloadAndUnpackContractsIfRequired(getExtension(), props)
		String antPattern = "${props.includedRootFolderAntPattern}*.*"
		String slashSeparatedGroupId = project.group.toString().replace(".", File.separator)
		String slashSeparatedAntPattern = antPattern.replace(slashSeparatedGroupId, project.group.toString())
		String root = OutputFolderBuilder.buildRootPath(project)
		ext.contractVerifierConfigProperties = props
		File outputContractsFolder = getExtension().stubsOutputDir != null ?
				project.file("${getExtension().stubsOutputDir}/${root}/contracts") :
				project.file("${project.buildDir}/stubs/${root}/contracts")
		ext.contractsDslDir = outputContractsFolder
		project.logger.info("Downloading and unpacking files from [$file] to [$outputContractsFolder]. The inclusion ant patterns are [${antPattern}] and [${slashSeparatedAntPattern}]")
		project.copy {
			from(file)
			// by default group id is slash separated...
			include(antPattern)
			// ...we also want to allow dot separation
			include(slashSeparatedAntPattern)
			if (props.isExcludeBuildFolders()) {
				exclude "**/target/**", "**/build/**"
			}
			into(outputContractsFolder)
		}
	}
}
