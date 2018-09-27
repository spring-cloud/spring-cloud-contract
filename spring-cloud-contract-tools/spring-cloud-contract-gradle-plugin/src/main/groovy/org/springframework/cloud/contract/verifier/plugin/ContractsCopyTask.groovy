/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.PackageScope
import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.Logger
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
		file = contractsSubDirIfPresent(logger, file)
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

	private File contractsSubDirIfPresent(Logger logger, File contractsDirectory) {
		File contracts = new File(contractsDirectory, "contracts")
		if (contracts.exists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Contracts folder found [" + contracts + "]")
			}
			contractsDirectory = contracts
		}
		return contractsDirectory
	}
}
