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

package org.springframework.cloud.contract.verifier.converter

import wiremock.com.google.common.collect.ListMultimap
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractFileScanner
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.util.NamesUtil
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Recursively converts contracts into their stub representations
 *
 * @since 1.1.0
 */
@Slf4j
@CompileStatic
class RecursiveFilesConverter {

	private final StubGeneratorProvider holder
	private final ContractVerifierConfigProperties properties
	private final File outMappingsDir

	RecursiveFilesConverter(ContractVerifierConfigProperties properties, StubGeneratorProvider holder = null) {
		this.properties = properties
		this.outMappingsDir = properties.stubsOutputDir
		this.holder = holder ?: new StubGeneratorProvider()
	}

	RecursiveFilesConverter(ContractVerifierConfigProperties properties, File outMappingsDir, StubGeneratorProvider holder = null) {
		this.properties = properties
		this.outMappingsDir = outMappingsDir
		this.holder = holder ?: new StubGeneratorProvider()
	}

	void processFiles() {
		ContractFileScanner scanner = new ContractFileScanner(properties.contractsDslDir,
				properties.excludedFiles as Set, [] as Set, properties.includedContracts)
		ListMultimap<Path, ContractMetadata> contracts = scanner.findContracts()
		if (log.isDebugEnabled()) {
			log.debug("Found the following contracts $contracts")
		}
		contracts.asMap().entrySet().each { entry ->
			entry.value.each { ContractMetadata contract ->
				if (log.isDebugEnabled()) {
					log.debug("Will create a stub for contract [${contract}]")
				}
				File sourceFile = contract.path.toFile()
				Collection<StubGenerator> stubGenerators = contract.convertedContract ? holder.allOrDefault(new DslToWireMockClientConverter()) :
						holder.converterForName(sourceFile.name)
				try {
					String path = sourceFile.path
					if (properties.isExcludeBuildFolders() && (matchesPath(path, "target") || matchesPath(path, "build"))) {
						if (log.isDebugEnabled()) {
							log.debug("Exclude build folder is set. Path [${path}] contains [target] or [build] in its path")
						}
						return
					}
					if (!contract.convertedContract && !stubGenerators) {
						return
					}
					int contractsSize = contract.convertedContract.size()
					def entryKey = entry.key
					if (log.isDebugEnabled()) {
						log.debug("Stub Generators [${stubGenerators}] will convert contents of [${entryKey}]")
					}
					stubGenerators.each { StubGenerator stubGenerator ->
						Map<Contract, String> convertedContent = stubGenerator.convertContents(entryKey.last().toString(), contract)
						if (!convertedContent) {
							return
						}
						convertedContent.entrySet().eachWithIndex { Map.Entry<Contract, String> content, int index ->
							Contract dsl = content.key
							String converted = content.value
                            if (converted) {
							    Path absoluteTargetPath = createAndReturnTargetDirectory(sourceFile)
							    File newJsonFile = createTargetFileWithProperName(stubGenerator, absoluteTargetPath,
							    		sourceFile, contractsSize, index, dsl)
							    newJsonFile.setText(converted, StandardCharsets.UTF_8.toString())
                            }
						}
					}
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

	private File createTargetFileWithProperName(StubGenerator stubGenerator, Path absoluteTargetPath,
												File sourceFile, int contractsSize, int index, Contract dsl) {
		String name = generateName(dsl, contractsSize, stubGenerator, sourceFile, index)
		File newJsonFile = new File(absoluteTargetPath.toFile(), name)
		log.info("Creating new stub [$newJsonFile.path]")
		return newJsonFile
	}

	private String generateName(Contract dsl, int contractsSize, StubGenerator converter,
								File sourceFile, int index) {
		String generatedName = converter.generateOutputFileNameForInput(sourceFile.name)
		String extension = NamesUtil.afterLastDot(generatedName)
		if (dsl.name) {
			return "${dsl.name}.${extension}"
		} else if (contractsSize == 1) {
			return generatedName
		}
		return "${index}_${generatedName}"
	}
}
