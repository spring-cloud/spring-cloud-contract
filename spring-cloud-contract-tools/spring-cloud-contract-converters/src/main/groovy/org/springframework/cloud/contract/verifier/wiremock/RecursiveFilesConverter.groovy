/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.wiremock

import com.google.common.collect.ListMultimap
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.converter.SingleFileConverter
import org.springframework.cloud.contract.verifier.file.ContractFileScanner
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
/**
 * Recursively converts contracts into their stub representations
 *
 * @since 1.0.0
 */
@Slf4j
@CompileStatic
class RecursiveFilesConverter {

	private final SingleFileConverter singleFileConverter
	private final ContractVerifierConfigProperties properties
	private final File outMappingsDir

	RecursiveFilesConverter(SingleFileConverter singleFileConverter, ContractVerifierConfigProperties properties) {
		this.properties = properties
		this.singleFileConverter = singleFileConverter
		this.outMappingsDir = properties.stubsOutputDir
	}

	RecursiveFilesConverter(SingleFileConverter singleFileConverter, ContractVerifierConfigProperties properties, File outMappingsDir) {
		this.properties = properties
		this.singleFileConverter = singleFileConverter
		this.outMappingsDir = outMappingsDir
	}

	void processFiles() {
		ContractFileScanner scanner = new ContractFileScanner(properties.contractsDslDir,
				properties.excludedFiles as Set, [] as Set, properties.includedContracts)
		ListMultimap<Path, ContractMetadata> contracts = scanner.findContracts()
		if (log.isDebugEnabled()) {
			log.debug("Found the following contracts ${contracts}")
			log.debug("Exclude build folder: [${properties.isExcludeBuildFolders()}]")
		}
		contracts.asMap().entrySet().each { entry ->
			entry.value.each { ContractMetadata contract ->
				File sourceFile = contract.path.toFile()
				try {
					String path = sourceFile.path
					if (properties.isExcludeBuildFolders() && (matchesPath(path, "target") || matchesPath(path, "build"))) {
						if (log.isDebugEnabled()) {
							log.debug("Exclude build folder is set. Path [${path}] contains [target] or [build] in its path")
						}
						return
					}
					if (!singleFileConverter.canHandleFileName(sourceFile.name)) {
						return
					}
					String convertedContent = singleFileConverter.convertContent(entry.key.last().toString(), contract)
					if (!convertedContent) {
						return
					}
					Path absoluteTargetPath = createAndReturnTargetDirectory(sourceFile)
					File newJsonFile = createTargetFileWithProperName(absoluteTargetPath, sourceFile)
					newJsonFile.setText(convertedContent, StandardCharsets.UTF_8.toString())
				} catch (Exception e) {
					throw new ConversionContractVerifierException("Unable to make conversion of ${sourceFile.name}", e)
				}
			}
		}
	}

	private boolean matchesPath(String path, String folder) {
		return path.matches("^.*${File.separator}${folder}${File.separator}.*\$")
	}

	private Path createAndReturnTargetDirectory(File sourceFile) {
		Path relativePath = Paths.get(properties.contractsDslDir.toURI()).relativize(sourceFile.parentFile.toPath())
		Path absoluteTargetPath = outMappingsDir.toPath().resolve(relativePath)
		Files.createDirectories(absoluteTargetPath)
		return absoluteTargetPath
	}

	private File createTargetFileWithProperName(Path absoluteTargetPath, File sourceFile) {
		File newJsonFile = new File(absoluteTargetPath.toFile(), singleFileConverter.generateOutputFileNameForInput(sourceFile.name))
		log.info("Creating new json [$newJsonFile.path]")
		return newJsonFile
	}
}
