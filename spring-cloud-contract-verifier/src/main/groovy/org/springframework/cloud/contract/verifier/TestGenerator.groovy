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

package org.springframework.cloud.contract.verifier

import com.google.common.collect.ListMultimap
import groovy.transform.PackageScope
import org.apache.commons.lang3.StringUtils
import org.springframework.cloud.contract.spec.ContractVerifierException
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractFileScanner
import org.springframework.cloud.contract.verifier.file.ContractMetadata

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import static org.springframework.cloud.contract.verifier.util.NamesUtil.*

/**
 * @author Jakub Kubrynski, codearte.io
 */
class TestGenerator {

	private final ContractVerifierConfigProperties configProperties
	private final String DEFAULT_CLASS_PREFIX = "ContractVerifier"

	private AtomicInteger counter = new AtomicInteger()
	private SingleTestGenerator generator
	private FileSaver saver
	private ContractFileScanner contractFileScanner

	TestGenerator(ContractVerifierConfigProperties configProperties) {
		this(configProperties, new SingleTestGenerator(configProperties),
				new FileSaver(configProperties.generatedTestSourcesDir, configProperties.targetFramework))
	}

	TestGenerator(ContractVerifierConfigProperties configProperties, SingleTestGenerator generator, FileSaver saver) {
		this.configProperties = configProperties
		if (configProperties.contractsDslDir == null) {
			throw new ContractVerifierException("Stubs directory not found under " + configProperties.contractsDslDir)
		}
		this.generator = generator
		this.saver = saver
		contractFileScanner = new ContractFileScanner(configProperties.contractsDslDir,
				configProperties.excludedFiles as Set,
				configProperties.ignoredFiles as Set,
				this.configProperties.includedContracts)
	}

	int generate() {
		if (!configProperties.basePackageForTests) {

		}
		generateTestClasses(configProperties.basePackageForTests)
		return counter.get()
	}

	@PackageScope
	void generateTestClasses(final String basePackageName) {
		ListMultimap<Path, ContractMetadata> contracts = contractFileScanner.findContracts()
		contracts.asMap().entrySet().each {
			Map.Entry<Path, Collection<ContractMetadata>> entry -> processIncludedDirectory(relativizeContractPath(entry), entry.getValue(), basePackageName)
		}
	}

	private String relativizeContractPath(Map.Entry<Path, Collection<Path>> entry) {
		Path relativePath = configProperties.contractsDslDir.toPath().relativize(entry.getKey())
		if (StringUtils.isBlank(relativePath.toString())) {
			return DEFAULT_CLASS_PREFIX
		}
		return relativePath.toString()
	}

	private void processIncludedDirectory(
			final String includedDirectoryRelativePath, Collection<ContractMetadata> contracts, final String basePackageNameForClass) {
		if (contracts.size()) {
			def className = afterLast(includedDirectoryRelativePath.toString(), File.separator) + resolveNameSuffix()
			def packageName = buildPackage(basePackageNameForClass, includedDirectoryRelativePath)
			def classBytes = generator.buildClass(contracts, className, packageName, includedDirectoryRelativePath).getBytes(StandardCharsets.UTF_8)
			saver.saveClassFile(className, basePackageNameForClass, convertIllegalPackageChars(includedDirectoryRelativePath.toString()), classBytes)
			counter.incrementAndGet()
		}
	}

	private String resolveNameSuffix() {
		return configProperties.nameSuffixForTests ?: configProperties.targetFramework.classNameSuffix
	}

	private static String buildPackage(final String packageNameForClass, final String includedDirectoryRelativePath) {
		String directory = beforeLast(includedDirectoryRelativePath, File.separator)
		return !directory.empty ? "$packageNameForClass.${directoryToPackage(convertIllegalPackageChars(directory))}" : packageNameForClass
	}

}