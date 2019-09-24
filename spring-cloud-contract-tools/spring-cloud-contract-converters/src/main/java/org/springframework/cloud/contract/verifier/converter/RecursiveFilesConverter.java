/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.converter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wiremock.com.google.common.collect.ListMultimap;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractFileScanner;
import org.springframework.cloud.contract.verifier.file.ContractFileScannerBuilder;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.cloud.contract.verifier.wiremock.DslToWireMockClientConverter;
import org.springframework.util.StringUtils;

/**
 * Recursively converts contracts into their stub representations.
 *
 * @since 1.1.0
 */
public class RecursiveFilesConverter {

	private static final Log log = LogFactory.getLog(RecursiveFilesConverter.class);

	private final StubGeneratorProvider holder;

	private final File outMappingsDir;

	private final File contractsDslDir;

	private final List<String> excludedFiles;

	private final String includedContracts;

	private final boolean excludeBuildFolders;

	@Deprecated
	public RecursiveFilesConverter(ContractVerifierConfigProperties props,
			StubGeneratorProvider holder) {
		this(props.getStubsOutputDir(), props.getContractsDslDir(),
				props.getExcludedFiles(), props.getIncludedContracts(),
				props.getExcludeBuildFolders(), holder);
	}

	@Deprecated
	public RecursiveFilesConverter(ContractVerifierConfigProperties props) {
		this(props.getStubsOutputDir(), props.getContractsDslDir(),
				props.getExcludedFiles(), props.getIncludedContracts(),
				props.getExcludeBuildFolders(), null);
	}

	@Deprecated
	public RecursiveFilesConverter(ContractVerifierConfigProperties props,
			File stubsOutputDir) {
		this(stubsOutputDir, props.getContractsDslDir(), props.getExcludedFiles(),
				props.getIncludedContracts(), props.getExcludeBuildFolders(), null);
	}

	@Deprecated
	public RecursiveFilesConverter(ContractVerifierConfigProperties props,
			File stubsOutputDir, StubGeneratorProvider holder) {
		this(stubsOutputDir, props.getContractsDslDir(), props.getExcludedFiles(),
				props.getIncludedContracts(), props.getExcludeBuildFolders(), holder);
	}

	public RecursiveFilesConverter(File stubsOutputDir, File contractsDslDir,
			List<String> excludedFiles, String includedContracts,
			boolean excludeBuildFolders, StubGeneratorProvider holder) {
		this.outMappingsDir = stubsOutputDir;
		this.contractsDslDir = contractsDslDir;
		this.excludedFiles = excludedFiles;
		this.includedContracts = includedContracts;
		this.excludeBuildFolders = excludeBuildFolders;
		this.holder = holder == null ? new StubGeneratorProvider() : holder;
	}

	public RecursiveFilesConverter(File stubsOutputDir, File contractsDslDir,
			List<String> excludedFiles, String includedContracts,
			boolean excludeBuildFolders) {
		this(stubsOutputDir, contractsDslDir, excludedFiles, includedContracts,
				excludeBuildFolders, null);
	}

	public void processFiles() {
		ContractFileScanner scanner = new ContractFileScannerBuilder()
				.baseDir(contractsDslDir).excluded(new HashSet<>(excludedFiles))
				.ignored(new HashSet<>()).included(new HashSet<>())
				.includeMatcher(includedContracts).build();
		ListMultimap<Path, ContractMetadata> contracts = scanner.findContracts();
		if (log.isDebugEnabled()) {
			log.debug("Found the following contracts " + contracts);
		}
		for (Map.Entry<Path, Collection<ContractMetadata>> entry : contracts.asMap()
				.entrySet()) {
			for (ContractMetadata contract : entry.getValue()) {
				if (log.isDebugEnabled()) {
					log.debug("Will create a stub for contract [" + contract + "]");
				}
				File sourceFile = contract.getPath().toFile();
				Collection<StubGenerator> stubGenerators = contract
						.getConvertedContract() != null
								? holder.allOrDefault(new DslToWireMockClientConverter())
								: holder.converterForName(sourceFile.getName());
				try {
					String path = sourceFile.getPath();
					if (excludeBuildFolders && (matchesPath(path, "target")
							|| matchesPath(path, "build"))) {
						if (log.isDebugEnabled()) {
							log.debug("Exclude build folder is set. Path [" + path
									+ "] contains [target] or [build] in its path");
						}

						continue;
					}
					if (nullOrEmpty(contract) && nullOrEmpty(stubGenerators)) {
						continue;
					}
					int contractsSize = contract.getConvertedContract().size();
					Path entryKey = entry.getKey();
					if (log.isDebugEnabled()) {
						log.debug("Stub Generators [" + stubGenerators
								+ "] will convert contents of [" + entryKey + "]");
					}

					for (StubGenerator stubGenerator : stubGenerators) {
						Map<Contract, String> convertedContent = stubGenerator
								.convertContents(last(entryKey).toString(), contract);
						if (convertedContent == null || convertedContent.isEmpty()) {
							continue;
						}
						Set<Map.Entry<Contract, String>> entrySet = convertedContent
								.entrySet();
						Iterator<Map.Entry<Contract, String>> iterator = entrySet
								.iterator();
						int index = 0;
						while (iterator.hasNext()) {
							Map.Entry<Contract, String> content = iterator.next();
							Contract dsl = content.getKey();
							String converted = content.getValue();
							if (StringUtils.hasText(converted)) {
								Path absoluteTargetPath = createAndReturnTargetDirectory(
										sourceFile);
								File newJsonFile = createTargetFileWithProperName(
										stubGenerator, absoluteTargetPath, sourceFile,
										contractsSize, index, dsl);
								Files.write(newJsonFile.toPath(),
										Collections.singletonList(converted),
										StandardCharsets.UTF_8);
							}
							index = index + 1;
						}

					}

				}
				catch (Exception e) {
					throw new ConversionContractVerifierException(
							"Unable to make conversion of " + sourceFile.getName(), e);
				}

			}

		}

	}

	private static <T> T last(Iterable<T> self) {
		Iterator<T> iterator = self.iterator();
		if (!iterator.hasNext()) {
			throw new NoSuchElementException(
					"Cannot access last() element from an empty Iterable");
		}
		T result = null;
		while (iterator.hasNext()) {
			result = iterator.next();
		}
		return result;
	}

	private boolean nullOrEmpty(ContractMetadata contract) {
		return contract.getConvertedContract() == null
				|| nullOrEmpty(contract.getConvertedContract());
	}

	private boolean nullOrEmpty(Collection collection) {
		return collection == null || collection.isEmpty();
	}

	private boolean matchesPath(String path, String folder) {
		return path.matches("^.*" + File.separator + folder + File.separator + ".*$");
	}

	private Path createAndReturnTargetDirectory(File sourceFile) {
		Path relativePath = Paths.get(contractsDslDir.toURI())
				.relativize(sourceFile.getParentFile().toPath());
		Path absoluteTargetPath = outMappingsDir.toPath().resolve(relativePath);
		try {
			Files.createDirectories(absoluteTargetPath);
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return absoluteTargetPath;
	}

	private File createTargetFileWithProperName(StubGenerator stubGenerator,
			Path absoluteTargetPath, File sourceFile, int contractsSize, int index,
			Contract dsl) {
		String name = generateName(dsl, contractsSize, stubGenerator, sourceFile, index);
		File newJsonFile = new File(absoluteTargetPath.toFile(), name);
		log.info("Creating new stub [" + newJsonFile.getPath() + "]");
		return newJsonFile;
	}

	private String generateName(Contract dsl, int contractsSize, StubGenerator converter,
			File sourceFile, int index) {
		String generatedName = converter
				.generateOutputFileNameForInput(sourceFile.getName());
		boolean hasDot = NamesUtil.hasDot(generatedName);
		String extension = hasDot ? NamesUtil.afterLastDot(generatedName) : "";
		if (StringUtils.hasText(dsl.getName()) && StringUtils.hasText(extension)) {
			return dsl.getName() + "." + extension;
		}
		else if (contractsSize == 1) {
			return generatedName;
		}
		return index + "_" + generatedName;
	}

}
