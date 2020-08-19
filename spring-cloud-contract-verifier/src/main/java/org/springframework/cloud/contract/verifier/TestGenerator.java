/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.ContractVerifierException;
import org.springframework.cloud.contract.verifier.builder.JavaTestGenerator;
import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractFileScanner;
import org.springframework.cloud.contract.verifier.file.ContractFileScannerBuilder;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.MultiValueMap;

import static org.springframework.cloud.contract.verifier.util.NamesUtil.afterLast;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.beforeLast;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.convertIllegalPackageChars;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.directoryToPackage;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.recrusiveDirectoryToPackage;
import static org.springframework.cloud.contract.verifier.util.NamesUtil.toLastDot;

/**
 * @author Jakub Kubrynski, codearte.io
 */
public class TestGenerator {

	private static final Logger log = LoggerFactory.getLogger(TestGenerator.class);
	private static final String DEFAULT_CLASS_PREFIX = "ContractVerifier";
	private static final String DEFAULT_TEST_PACKAGE = "org.springframework.cloud.contract.verifier.tests";

	private final ContractVerifierConfigProperties configProperties;
	private final AtomicInteger counter = new AtomicInteger();
	private final SingleTestGenerator generator;
	private final FileSaver saver;
	private final ContractFileScanner contractFileScanner;

	public TestGenerator(ContractVerifierConfigProperties configProperties) {
		this(configProperties, singleTestGenerator(),
				new FileSaver(configProperties.getGeneratedTestSourcesDir(),
						configProperties.getTestFramework().getClassExtension()));
	}

	private static SingleTestGenerator singleTestGenerator() {
		List<SingleTestGenerator> factories = SpringFactoriesLoader
				.loadFactories(SingleTestGenerator.class, null);
		if (factories.isEmpty()) {
			return new JavaTestGenerator();
		}
		return factories.get(0);
	}

	public TestGenerator(ContractVerifierConfigProperties configProperties,
			SingleTestGenerator generator, FileSaver saver) {
		this(configProperties, generator, saver,
				new ContractFileScannerBuilder()
						.baseDir(configProperties.getContractsDslDir())
						.excluded(new HashSet<>(configProperties.getExcludedFiles()))
						.ignored(new HashSet<>(configProperties.getIgnoredFiles()))
						.included(new HashSet<>(configProperties.getIncludedFiles()))
						.includeMatcher(configProperties.getIncludedContracts()).build());
	}

	protected TestGenerator(ContractVerifierConfigProperties configProperties,
			SingleTestGenerator generator, FileSaver saver,
			ContractFileScanner contractFileScanner) {
		this.configProperties = configProperties;
		if (configProperties.getContractsDslDir() == null) {
			throw new ContractVerifierException("Stubs directory not found under "
					+ configProperties.getContractsDslDir());
		}

		this.generator = generator;
		this.saver = saver;
		this.contractFileScanner = contractFileScanner;
	}

	public int generate() {
		generateTestClasses(basePackageName());
		recrusiveDirectoryToPackage(configProperties.getGeneratedTestSourcesDir());
		recrusiveDirectoryToPackage(configProperties.getGeneratedTestResourcesDir());
		return counter.get();
	}

	private String basePackageName() {
		if (StringUtils.isNotEmpty(configProperties.getBasePackageForTests())) {
			return configProperties.getBasePackageForTests();
		}
		else if (StringUtils.isNotEmpty(configProperties.getBaseClassForTests())) {
			return toLastDot(configProperties.getBaseClassForTests());
		}
		else if (StringUtils.isNotEmpty(configProperties.getPackageWithBaseClasses())) {
			return configProperties.getPackageWithBaseClasses();
		}
		return DEFAULT_TEST_PACKAGE;
	}

	void generateTestClasses(final String basePackageName) {
		MultiValueMap<Path, ContractMetadata> contracts = contractFileScanner
				.findContractsRecursively();
		log.debug("Found the following contracts {}", contracts.keySet());

		Set<Map.Entry<Path, List<ContractMetadata>>> inProgress = inProgress(contracts);
		if (!inProgress.isEmpty() && configProperties.isFailOnInProgress()) {
			String inProgressContractsPaths = inProgress.stream().map(Map.Entry::getKey)
					.map(Path::toString).collect(Collectors.joining(","));
			throw new IllegalStateException("In progress contracts found in paths ["
					+ inProgressContractsPaths
					+ "] and the switch [failOnInProgress] is set to [true]. Either unmark those contracts as in progress, or set the switch to [false].");
		}
		processAllNotInProgress(contracts, basePackageName);
	}

	private Set<Map.Entry<Path, List<ContractMetadata>>> inProgress(
			MultiValueMap<Path, ContractMetadata> contracts) {
		return contracts.entrySet().stream()
				.filter(entry -> entry.getValue().stream()
						.anyMatch(ContractMetadata::anyInProgress))
				.collect(Collectors.toSet());
	}

	void processAllNotInProgress(MultiValueMap<Path, ContractMetadata> contracts,
			final String basePackageName) {
		contracts.entrySet().stream()
				.filter(entry -> entry.getValue().stream()
						.noneMatch(ContractMetadata::anyInProgress))
				.forEach(entry -> processIncludedDirectory(relativizeContractPath(entry),
						entry.getValue(), basePackageName));
	}

	private String relativizeContractPath(Map.Entry<Path, List<ContractMetadata>> entry) {
		Path relativePath = configProperties.getContractsDslDir().toPath()
				.relativize(entry.getKey());
		return StringUtils.defaultIfEmpty(relativePath.toString(), DEFAULT_CLASS_PREFIX);
	}

	private void processIncludedDirectory(final String includedDirectoryRelativePath,
			final Collection<ContractMetadata> contracts,
			final String basePackageNameForClass) {
		log.debug("Collected contracts with metadata {} relative path is [{}]", contracts,
				includedDirectoryRelativePath);
		if (!contracts.isEmpty()) {
			String className = afterLast(includedDirectoryRelativePath, File.separator)
					+ resolveNameSuffix();
			String convertedClassName = convertIllegalPackageChars(className);
			String packageName = buildPackage(basePackageNameForClass,
					includedDirectoryRelativePath);
			Path dir = saver.generateTestBaseDir(basePackageNameForClass,
					convertIllegalPackageChars(includedDirectoryRelativePath));
			Path classPath = saver.pathToClass(dir, convertedClassName);
			byte[] classBytes = generator
					.buildClass(configProperties, contracts,
							includedDirectoryRelativePath,
							new SingleTestGenerator.GeneratedClassData(convertedClassName,
									packageName, classPath))
					.getBytes(StandardCharsets.UTF_8);
			saver.saveClassFile(classPath, classBytes);
			counter.incrementAndGet();
		}
	}

	private String resolveNameSuffix() {
		return StringUtils.defaultIfEmpty(configProperties.getNameSuffixForTests(),
				configProperties.getTestFramework().getClassNameSuffix());
	}

	protected static String buildPackage(final String packageNameForClass,
			final String includedDirectoryRelativePath) {
		String directory = beforeLast(includedDirectoryRelativePath, File.separator);
		String convertedPackage = packageNameForClass + "."
				+ directoryToPackage(convertIllegalPackageChars(directory));
		return !directory.isEmpty() ? convertedPackage : packageNameForClass;
	}

}
