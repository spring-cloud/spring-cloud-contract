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

package org.springframework.cloud.contract.verifier.file;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractConverter;
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Scans the provided file path for the DSLs. There's a possibility to provide inclusion
 * and exclusion filters.
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Stessy Delcroix
 * @since 1.0.0
 */
public class ContractFileScanner {

	private static final Logger LOG = LoggerFactory.getLogger(ContractFileScanner.class);

	private static final String OS_NAME = System.getProperty("os.name");

	private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

	protected static final boolean IS_OS_WINDOWS = getOSMatchesName(OS_NAME_WINDOWS_PREFIX);

	private static final String MATCH_PREFIX = "glob:";

	private static final Pattern SCENARIO_STEP_FILENAME_PATTERN = Pattern.compile("[0-9]+_.*");

	private final File baseDir;

	private final Set<PathMatcher> excludeMatchers;

	private final Set<PathMatcher> ignoreMatchers;

	private final Set<PathMatcher> includeMatchers;

	private final String includeMatcher;

	public ContractFileScanner(File baseDir, Set<String> excluded, Set<String> ignored, Set<String> included,
			String includeMatcher) {
		this.baseDir = baseDir;
		this.excludeMatchers = processPatterns(excluded != null ? excluded : Collections.emptySet());
		this.ignoreMatchers = processPatterns(ignored != null ? ignored : Collections.emptySet());
		this.includeMatchers = processPatterns(included != null ? included : Collections.emptySet());
		this.includeMatcher = includeMatcher != null ? includeMatcher : "";
	}

	private Set<PathMatcher> processPatterns(Set<String> patterns) {
		FileSystem fileSystem = FileSystems.getDefault();
		Set<PathMatcher> pathMatchers = new HashSet<>();
		for (String pattern : patterns) {
			String syntaxAndPattern = MATCH_PREFIX + "**" + File.separator + pattern;
			// FIXME: This looks strange, need to be checked on windows
			if (IS_OS_WINDOWS) {
				syntaxAndPattern = syntaxAndPattern.replace("\\", "\\\\");
			}
			pathMatchers.add(fileSystem.getPathMatcher(syntaxAndPattern));
		}
		return pathMatchers;
	}

	public MultiValueMap<Path, ContractMetadata> findContractsRecursively() {
		MultiValueMap<Path, ContractMetadata> result = CollectionUtils.toMultiValueMap(new LinkedHashMap<>());
		appendRecursively(baseDir, result);
		return result;
	}

	/**
	 * We iterate over found contracts, filter out those that should be excluded and try
	 * to convert via pluggable Contract Converters any possible contracts.
	 */
	private void appendRecursively(File baseDir, MultiValueMap<Path, ContractMetadata> result) {
		List<ContractConverter> converters = convertersWithYml();
		if (LOG.isTraceEnabled()) {
			LOG.trace("Found the following contract converters " + converters);
		}
		File[] files = baseDir.listFiles();
		if (files == null) {
			return;
		}
		Arrays.sort(files);
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			boolean excluded = matchesPattern(file, excludeMatchers);
			if (!excluded) {
				boolean contractFile = isContractFile(file);
				boolean included = !StringUtils.hasText(includeMatcher)
						|| file.getAbsolutePath().matches(includeMatcher);
				included = !CollectionUtils.isEmpty(includeMatchers) ? matchesPattern(file, includeMatchers) : included;
				if (contractFile && included) {
					addContractToTestGeneration(result, files, file, i,
							ContractVerifierDslConverter.convertAsCollection(baseDir, file));
				}
				if (!contractFile && included) {
					addContractToTestGeneration(converters, result, files, file, i);
				}
				else {
					appendRecursively(file, result);
					if (LOG.isDebugEnabled()) {
						LOG.debug("File [" + file + "] is ignored. Is a contract file? [" + contractFile
								+ "]. Should be included by pattern? [" + included + "]");
					}
				}
			}
			else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("File [" + file + "] is ignored. Should be excluded? [" + excluded + "]");
				}
			}
		}
	}

	protected List<ContractConverter> convertersWithYml() {
		List<ContractConverter> converters = converters();
		converters.add(ContractVerifierDslConverter.INSTANCE);
		converters.add(YamlContractConverter.INSTANCE);
		return converters;
	}

	protected List<ContractConverter> converters() {
		return SpringFactoriesLoader.loadFactories(ContractConverter.class, null);
	}

	private void addContractToTestGeneration(List<ContractConverter> converters,
			MultiValueMap<Path, ContractMetadata> result, File[] files, File file, int index) {
		boolean converted = false;
		if (!file.isDirectory()) {
			for (ContractConverter converter : converters) {
				Collection<Contract> contracts = tryConvert(converter, file);
				if (contracts != null) {
					addContractToTestGeneration(result, files, file, index, contracts);
					converted = true;
					break;
				}
			}
		}
		if (!converted) {
			appendRecursively(file, result);
			if (LOG.isDebugEnabled()) {
				LOG.debug(
						"File [" + file + "] wasn't ignored but no converter was applicable. The file is a directory ["
								+ file.isDirectory() + "]");
			}
		}
	}

	private Collection<Contract> tryConvert(ContractConverter converter, File file) {
		boolean accepted = converter.isAccepted(file);
		if (!accepted) {
			return null;
		}
		try {
			return converter.convertFrom(file);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to convert file [" + file + "]", e);
		}
	}

	private void addContractToTestGeneration(MultiValueMap<Path, ContractMetadata> result, File[] files, File file,
			int index, Collection<Contract> convertedContract) {
		Path path = file.toPath();
		Integer order = null;
		if (hasScenarioFilenamePattern(path)) {
			order = index;
		}
		Path parent = file.getParentFile().toPath();
		ContractMetadata metadata = new ContractMetadata(path, matchesPattern(file, ignoreMatchers), files.length,
				order, convertedContract);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating a contract entry for path [" + path + "] and metadata [" + metadata + "]");
		}
		result.add(parent, metadata);
	}

	private boolean hasScenarioFilenamePattern(Path path) {
		return SCENARIO_STEP_FILENAME_PATTERN.matcher(path.getFileName().toString()).matches();
	}

	private boolean matchesPattern(File file, Set<PathMatcher> matchers) {
		for (PathMatcher matcher : matchers) {
			if (matcher.matches(file.toPath())) {
				return true;
			}
		}
		return false;
	}

	private boolean isContractFile(File file) {
		return file.isFile() && ContractVerifierDslConverter.INSTANCE.isAccepted(file);
	}

	/**
	 * Decides if the operating system matches.
	 * @param osNamePrefix the prefix for the os name
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean getOSMatchesName(final String osNamePrefix) {
		return isOSNameMatch(OS_NAME, osNamePrefix);
	}

	/**
	 * Decides if the operating system matches.
	 * <p>
	 * This method is package private instead of private to support unit test invocation.
	 * </p>
	 * @param osName the actual OS name
	 * @param osNamePrefix the prefix for the expected OS name
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean isOSNameMatch(final String osName, final String osNamePrefix) {
		if (osName == null) {
			return false;
		}
		return osName.startsWith(osNamePrefix);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private File baseDir;

		private Set<String> excluded;

		private Set<String> ignored;

		private Set<String> included = Collections.emptySet();

		private String includeMatcher = "";

		public Builder baseDir(File baseDir) {
			this.baseDir = baseDir;
			return this;
		}

		public Builder excluded(Set<String> excluded) {
			this.excluded = excluded;
			return this;
		}

		public Builder ignored(Set<String> ignored) {
			this.ignored = ignored;
			return this;
		}

		public Builder included(Set<String> included) {
			this.included = included;
			return this;
		}

		public Builder includeMatcher(String includeMatcher) {
			this.includeMatcher = includeMatcher;
			return this;
		}

		public ContractFileScanner build() {
			return new ContractFileScanner(this.baseDir, this.excluded, this.ignored, this.included,
					this.includeMatcher);
		}

	}

}
