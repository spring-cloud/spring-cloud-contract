/*
 *  Copyright 2013-2016 the original author or authors.
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
import java.nio.file.StandardCopyOption
/**
 * Recursively converts contracts into their stub representations
 *
 * @since 1.0.0
 */
@Slf4j
@CompileStatic
class RecursiveFilesConverter {

	private static final String DEFAULT_CONTRACTS_FOLDER = "contracts"
	private static final String DEFAULT_MAPPINGS_FOLDER = "mappings"
	private final SingleFileConverter singleFileConverter
	private final ContractVerifierConfigProperties properties

	RecursiveFilesConverter(SingleFileConverter singleFileConverter, ContractVerifierConfigProperties properties) {
		this.properties = properties
		this.singleFileConverter = singleFileConverter
	}

	void processFiles() {
		ContractFileScanner scanner = new ContractFileScanner(properties.contractsDslDir, properties.excludedFiles as Set, [] as Set)
		ListMultimap<Path, ContractMetadata> contracts = scanner.findContracts()
		log.debug("Found the following contracts $contracts")
		createContractDirInOutputStubsFolder()
		contracts.asMap().entrySet().each { entry ->
			entry.value.each { ContractMetadata contract ->
				File sourceFile = contract.path.toFile()
				try {
					if (!singleFileConverter.canHandleFileName(sourceFile.name)) {
						return
					}
					Path targetContractDir = createAndReturnTargetDirectory(sourceFile, DEFAULT_CONTRACTS_FOLDER)
					Files.copy(sourceFile.toPath(), new File(targetContractDir.toFile(), sourceFile.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
					String convertedContent = singleFileConverter.convertContent(entry.key.last().toString(), contract)
					if (!convertedContent) {
						return
					}
					Path absoluteTargetPath = createAndReturnTargetDirectory(sourceFile, DEFAULT_MAPPINGS_FOLDER)
					File newJsonFile = createTargetFileWithProperName(absoluteTargetPath, sourceFile)
					newJsonFile.setText(convertedContent, StandardCharsets.UTF_8.toString())
				} catch (Exception e) {
					throw new ConversionContractVerifierException("Unable to make conversion of ${sourceFile.name}", e)
				}
			}
		}
	}

	private boolean createContractDirInOutputStubsFolder() {
		return contractDir().mkdirs()
	}

	private File contractDir() {
		return new File(properties.stubsOutputDir, DEFAULT_CONTRACTS_FOLDER)
	}

	private Path createAndReturnTargetDirectory(File sourceFile, String defaultFolder) {
		Path relativePath = Paths.get(properties.contractsDslDir.toURI()).relativize(sourceFile.parentFile.toPath())
		Path absoluteTargetPath = new File(properties.stubsOutputDir, defaultFolder).toPath().resolve(relativePath)
		Files.createDirectories(absoluteTargetPath)
		return absoluteTargetPath
	}

	private File createTargetFileWithProperName(Path absoluteTargetPath, File sourceFile) {
		File newJsonFile = new File(absoluteTargetPath.toFile(), singleFileConverter.generateOutputFileNameForInput(sourceFile.name))
		log.info("Creating new json [$newJsonFile.path]")
		return newJsonFile
	}
}
