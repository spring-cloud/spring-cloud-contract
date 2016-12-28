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
		ext.contractVerifierConfigProperties = props
		File outputContractsFolder = getExtension().stubsOutputDir != null ?
				project.file("${getExtension().stubsOutputDir}/contracts") :
				project.file("${project.buildDir}/stubs/contracts")
		ext.contractsDslDir = outputContractsFolder
		project.logger.info("Downloading and unpacking files from [$file] to [$outputContractsFolder]. The inclusion ant pattern is [$antPattern]")
		project.copy {
			from(file)
			include(antPattern)
			if (props.isExcludeBuildFolders()) {
				exclude "**/target/**", "**/build/**"
			}
			into(outputContractsFolder)
		}
	}
}
