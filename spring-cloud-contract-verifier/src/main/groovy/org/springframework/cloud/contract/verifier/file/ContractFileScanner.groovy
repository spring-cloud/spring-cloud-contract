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

package org.springframework.cloud.contract.verifier.file

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import wiremock.com.google.common.collect.HashMultiset
import wiremock.com.google.common.collect.ListMultimap
import wiremock.com.google.common.collect.Multimap
import wiremock.com.google.common.collect.Multiset

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.core.io.support.SpringFactoriesLoader
import org.springframework.util.CollectionUtils
import org.springframework.util.MultiValueMap
/**
 * Scans the provided file path for the DSLs. There's a possibility to provide
 * inclusion and exclusion filters.
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
@CompileStatic
@Commons
class ContractFileScanner {

	private static final String OS_NAME = System.getProperty("os.name")
	private static final String OS_NAME_WINDOWS_PREFIX = "Windows"
	protected static final boolean IS_OS_WINDOWS =
			getOSMatchesName(OS_NAME_WINDOWS_PREFIX)

	private static final String MATCH_PREFIX = "glob:"
	private static final Pattern SCENARIO_STEP_FILENAME_PATTERN = Pattern.
			compile("[0-9]+_.*")
	private final File baseDir
	private final Set<PathMatcher> excludeMatchers
	private final Set<PathMatcher> ignoreMatchers
	private final Set<PathMatcher> includeMatchers
	private final String includeMatcher

	ContractFileScanner(File baseDir, Set<String> excluded, Set<String> ignored,
			Set<String> included = [],
			String includeMatcher = "") {
		this.baseDir = baseDir
		this.excludeMatchers = processPatterns(excluded ?: [] as Set<String>)
		this.ignoreMatchers = processPatterns(ignored ?: [] as Set<String>)
		this.includeMatchers = processPatterns(included ?: [] as Set<String>)
		this.includeMatcher = includeMatcher
	}

	private Set<PathMatcher> processPatterns(Set<String> patterns) {
		FileSystem fileSystem = FileSystems.getDefault()
		Set<PathMatcher> pathMatchers = new HashSet<PathMatcher>()
		for (String pattern : patterns) {
			String syntaxAndPattern = MATCH_PREFIX + '**' + File.separator + pattern
			// FIXME: This looks strange, need to be checked on windows
			if (IS_OS_WINDOWS) {
				syntaxAndPattern = syntaxAndPattern.replace("\\", "\\\\")
			}
			pathMatchers.add(fileSystem.getPathMatcher(syntaxAndPattern))
		}
		return pathMatchers
	}

	/**
	 * @return for a map of paths for which a list of matching contracts has been found
	 * @deprecated use the {@link ContractFileScanner#findContractsRecursively} version
	 */
	@Deprecated
	ListMultimap<Path, ContractMetadata> findContracts() {
		MultiValueMap<Path, ContractMetadata> contracts = findContractsRecursively();
		return new ListMultimap<Path, ContractMetadata>() {
			@Override
			List<ContractMetadata> get(Path key) {
				return contracts.get(key)
			}

			@Override
			List<ContractMetadata> removeAll(Object key) {
				return contracts.remove(key)
			}

			@Override
			List<ContractMetadata> replaceValues(Path key, Iterable<? extends ContractMetadata> values) {
				return contracts.put(key, asList(values))
			}

			private static List<? extends ContractMetadata> asList(Iterable<? extends ContractMetadata> self) {
				if (self instanceof List) {
					return (List<? extends ContractMetadata>) self;
				} else {
					return toList(self.iterator());
				}
			}

			private static List<? extends ContractMetadata> toList(Iterator<? extends ContractMetadata> self) {
				List<? extends ContractMetadata> answer = new ArrayList<>();
				while (self.hasNext()) {
					answer.add(self.next());
				}
				return answer;
			}

			@Override
			Map<Path, Collection<ContractMetadata>> asMap() {
				return contracts.collectEntries {
					[(it.key): (Collection<ContractMetadata>) it.value]
				} as Map<Path, Collection<ContractMetadata>>
			}

			@Override
			int size() {
				return contracts.size()
			}

			@Override
			boolean isEmpty() {
				return contracts.isEmpty()
			}

			@Override
			boolean containsKey(Object key) {
				return contracts.containsKey(key)
			}

			@Override
			boolean containsValue(Object value) {
				return contracts.findResult { it.value.contains(value) }
			}

			@Override
			boolean containsEntry(Object key, Object value) {
				return contracts.findResult { it.key == key && it.value.contains(value) }
			}

			@Override
			boolean put(Path key, ContractMetadata value) {
				return contracts.add(key, value)
			}

			@Override
			boolean remove(Object key, Object value) {
				return contracts.getOrDefault(key, new ArrayList<ContractMetadata>()).remove(value)
			}

			@Override
			boolean putAll(Path key, Iterable<? extends ContractMetadata> values) {
				return contracts.getOrDefault(key, new ArrayList<ContractMetadata>()).addAll(values)
			}

			@Override
			boolean putAll(Multimap<? extends Path, ? extends ContractMetadata> multimap) {
				multimap.entries().each {
					contracts.add(it.key, it.value)
				}
				return true
			}

			@Override
			void clear() {
				contracts.clear()
			}

			@Override
			Set<Path> keySet() {
				return contracts.keySet()
			}

			@Override
			Multiset<Path> keys() {
				return HashMultiset.create(contracts.keySet())
			}

			@Override
			Collection<ContractMetadata> values() {
				return (Collection<ContractMetadata>) contracts.values().flatten()
			}

			@Override
			Collection<Map.Entry<Path, ContractMetadata>> entries() {
				Collection<Map.Entry<Path, ContractMetadata>> entries = new LinkedList<>()
				contracts.each {
					Path path = it.key
					List<ContractMetadata> list = it.value
					list.each {
						entries.add(new AbstractMap.SimpleEntry<Path, ContractMetadata>(path, it))
					}
				}
				return entries
			}
		}
	}


	MultiValueMap<Path, ContractMetadata> findContractsRecursively() {
		MultiValueMap<Path, ContractMetadata> result = CollectionUtils.toMultiValueMap(new LinkedHashMap<>());
		appendRecursively(baseDir, result)
		return result
	}

	/**
	 * We iterate over found contracts, filter out those that should be excluded
	 * and try to convert via pluggable Contract Converters any possible contracts
	 */
	private void appendRecursively(File baseDir, MultiValueMap<Path, ContractMetadata> result) {
		List<ContractConverter> converters = convertersWithYml()
		if (log.isTraceEnabled()) {
			log.trace("Found the following contract converters ${converters}")
		}
		File[] files = baseDir.listFiles()
		if (!files) {
			return
		}
		File[] sortedFiles = files.sort() as File[]
		for (int i = 0; i < sortedFiles.length; i++) {
			File file = sortedFiles[i]
			boolean excluded = matchesPattern(file, excludeMatchers)
			if (!excluded) {
				boolean contractFile = isContractFile(file)
				boolean included = includeMatcher ? file.absolutePath.
						matches(includeMatcher) : true
				included = includeMatchers ?
						matchesPattern(file, includeMatchers) : included
				if (contractFile && included) {
					addContractToTestGeneration(result, files, file, i, ContractVerifierDslConverter.
							convertAsCollection(baseDir, file))
				}
				if (!contractFile && included) {
					addContractToTestGeneration(converters, result, files, file, i)
				}
				else {
					appendRecursively(file, result)
					if (log.isDebugEnabled()) {
						log.debug("File [$file] is ignored. Is a contract file? [$contractFile]. Should be included by pattern? [$included]")
					}
				}
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("File [$file] is ignored. Should be excluded? [$excluded]")
				}
			}
		}
	}

	protected List<ContractConverter> convertersWithYml() {
		List<ContractConverter> converters = converters()
		converters.add(ContractVerifierDslConverter.INSTANCE)
		converters.add(YamlContractConverter.INSTANCE)
		return converters
	}

	protected List<ContractConverter> converters() {
		return SpringFactoriesLoader.loadFactories(ContractConverter, null)
	}

	private void addContractToTestGeneration(List<ContractConverter> converters, MultiValueMap<Path, ContractMetadata> result,
			File[] files, File file, int index) {
		boolean converted = false
		if (!file.isDirectory()) {
			for (ContractConverter converter : converters) {
				Collection<Contract> contracts = tryConvert(converter, file)
				if (contracts) {
					addContractToTestGeneration(result, files, file, index, contracts)
					converted = true
					break
				}
			}
		}
		if (!converted) {
			appendRecursively(file, result)
			if (log.isDebugEnabled()) {
				log.debug("File [$file] wasn't ignored but no converter was applicable. The file is a directory [${file.isDirectory()}]")
			}
		}
	}

	private Collection<Contract> tryConvert(ContractConverter converter, File file) {
		boolean accepted = converter.isAccepted(file)
		if (!accepted) {
			return null
		}
		try {
			return converter.convertFrom(file)
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to convert file [" + file + "]", e)
		}
	}

	private void addContractToTestGeneration(MultiValueMap<Path, ContractMetadata> result, File[] files, File file,
			int index, Collection<Contract> convertedContract) {
		Path path = file.toPath()
		Integer order = null
		if (hasScenarioFilenamePattern(path)) {
			order = index
		}
		Path parent = file.parentFile.toPath()
		ContractMetadata metadata = new ContractMetadata(path,
				matchesPattern(file, ignoreMatchers),
				files.size(), order, convertedContract)
		if (log.isDebugEnabled()) {
			log.debug("Creating a contract entry for path [" + path + "] and metadata [" + metadata + "]")
		}
		result.add(parent, metadata)
	}

	private boolean hasScenarioFilenamePattern(Path path) {
		return SCENARIO_STEP_FILENAME_PATTERN.matcher(path.fileName.toString()).matches()
	}

	private boolean matchesPattern(File file, Set<PathMatcher> matchers) {
		for (PathMatcher matcher : matchers) {
			if (matcher.matches(file.toPath())) {
				return true
			}
			log.debug("Path [${file.toPath()}] doesn't match the pattern [${matcher}]")
		}
		return false
	}

	private boolean isContractFile(File file) {
		return file.isFile() && ContractVerifierDslConverter.INSTANCE.isAccepted(file)
	}

	/**
	 * Decides if the operating system matches.
	 *
	 * @param osNamePrefix the prefix for the os name
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean getOSMatchesName(final String osNamePrefix) {
		return isOSNameMatch(OS_NAME, osNamePrefix)
	}

	/**
	 * Decides if the operating system matches.
	 * <p>
	 * This method is package private instead of private to support unit test invocation.
	 * </p>
	 *
	 * @param osName the actual OS name
	 * @param osNamePrefix the prefix for the expected OS name
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean isOSNameMatch(final String osName, final String osNamePrefix) {
		if (osName == null) {
			return false
		}
		return osName.startsWith(osNamePrefix)
	}
}
