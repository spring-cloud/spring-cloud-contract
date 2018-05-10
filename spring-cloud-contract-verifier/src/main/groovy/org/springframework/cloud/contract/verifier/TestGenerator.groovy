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

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import groovy.transform.PackageScope
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import wiremock.com.google.common.collect.ListMultimap

import org.springframework.cloud.contract.spec.ContractVerifierException
import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractFileScanner
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.core.io.support.SpringFactoriesLoader
import org.springframework.util.StringUtils

import static org.springframework.cloud.contract.verifier.util.NamesUtil.afterLast
import static org.springframework.cloud.contract.verifier.util.NamesUtil.beforeLast
import static org.springframework.cloud.contract.verifier.util.NamesUtil.convertIllegalPackageChars
import static org.springframework.cloud.contract.verifier.util.NamesUtil.directoryToPackage
import static org.springframework.cloud.contract.verifier.util.NamesUtil.toLastDot
/**
 * @author Jakub Kubrynski, codearte.io
 */
class TestGenerator {

	private static final Log log = LogFactory.getLog(TestGenerator)
	private static final String DEFAULT_CLASS_PREFIX = "ContractVerifier"
	private static final String DEFAULT_TEST_PACKAGE = "org.springframework.cloud.contract.verifier.tests"

	private final ContractVerifierConfigProperties configProperties
	private AtomicInteger counter = new AtomicInteger()
	private SingleTestGenerator generator
	private FileSaver saver
	private ContractFileScanner contractFileScanner

	TestGenerator(ContractVerifierConfigProperties configProperties) {
		this(configProperties, singleTestGenerator(),
				new FileSaver(configProperties.generatedTestSourcesDir, singleTestGenerator(), configProperties))
	}

	private static SingleTestGenerator singleTestGenerator() {
		List<SingleTestGenerator> factories = SpringFactoriesLoader.loadFactories(SingleTestGenerator, null)
		if (factories.empty) {
			return new JavaTestGenerator()
		}
		return factories.first()
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
				configProperties.includedFiles as Set,
				this.configProperties.includedContracts)
	}

	int generate() {
		generateTestClasses(basePackageName())
		return counter.get()
	}

	private String basePackageName() {
		if (configProperties.basePackageForTests) {
			return configProperties.basePackageForTests
		} else if (configProperties.baseClassForTests) {
			return toLastDot(configProperties.baseClassForTests)
		} else if (configProperties.packageWithBaseClasses) {
			return configProperties.packageWithBaseClasses
		}
		return DEFAULT_TEST_PACKAGE
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
		if (StringUtils.isEmpty(relativePath.toString())) {
			return DEFAULT_CLASS_PREFIX
		}
		return relativePath.toString()
	}

	private void processIncludedDirectory(
			final String includedDirectoryRelativePath, Collection<ContractMetadata> contracts, final String basePackageNameForClass) {
		if (log.isDebugEnabled()) {
			log.debug("Collected contracts with metadata ${contracts}")
		}
		if (contracts.size()) {
			def className = afterLast(includedDirectoryRelativePath.toString(), File.separator) + resolveNameSuffix()
			def convertedClassName = convertIllegalPackageChars(className)
			def packageName = buildPackage(basePackageNameForClass, includedDirectoryRelativePath)
			def classBytes = generator.buildClass(configProperties, contracts, convertedClassName, packageName, includedDirectoryRelativePath).getBytes(StandardCharsets.UTF_8)
			saver.saveClassFile(convertedClassName, basePackageNameForClass, convertIllegalPackageChars(includedDirectoryRelativePath.toString()), classBytes)
			counter.incrementAndGet()
		}
	}

	private String resolveNameSuffix() {
		return configProperties.nameSuffixForTests ?: configProperties.targetFramework.classNameSuffix
	}

	protected static String buildPackage(final String packageNameForClass, final String includedDirectoryRelativePath) {
		String directory = beforeLast(includedDirectoryRelativePath, File.separator)
		String convertedPackage = "$packageNameForClass.${directoryToPackage(convertIllegalPackageChars(directory))}"
		return !directory.empty ? convertedPackage : packageNameForClass
	}

}