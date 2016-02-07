package io.codearte.accurest.file

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import org.apache.commons.io.FilenameUtils

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * @author Jakub Kubrynski
 */
class ContractFileScanner {

	private final String MATCH_PREFIX = "glob:"
	private final File baseDir
	private final Set<PathMatcher> excludeMatchers
	private final Set<PathMatcher> ignoreMatchers

	ContractFileScanner(File baseDir, Set<String> excluded, Set<String> ignored) {
		this.baseDir = baseDir
		excludeMatchers = processPatterns(excluded, baseDir)
		ignoreMatchers = processPatterns(ignored, baseDir)
	}

	private Set<PathMatcher> processPatterns(Set<String> patterns, baseDir) {
		FileSystem fileSystem = FileSystems.getDefault()
		return patterns.collect({
			fileSystem.getPathMatcher(MATCH_PREFIX + baseDir.toString() + File.separator + it)
		}) as Set
	}

	Multimap<Path, Contract> findContracts() {
		Multimap<Path, Contract> result = ArrayListMultimap.create()
		appendRecursively(baseDir, result)
		return result
	}

	private void appendRecursively(File baseDir, Multimap<Path, Contract> result) {
		for (File file : baseDir.listFiles()) {
			if (matchesPattern(file, excludeMatchers)) {
				break;
			}
			if (isContractFile(file)) {
				Path path = file.toPath()
				result.put(file.parentFile.toPath(), new Contract(path, matchesPattern(file, ignoreMatchers)))
			} else {
				appendRecursively(file, result)
			}
		}
	}

	boolean matchesPattern(File file, Set<PathMatcher> excludeMatchers) {
		for (PathMatcher matcher : excludeMatchers) {
			if (matcher.matches(file.toPath())) {
				return true;
			}
		}
		return false;
	}

	private boolean isContractFile(File file) {
		file.isFile() && FilenameUtils.getExtension(file.toString()).equalsIgnoreCase("groovy")
	}
}
