package io.codearte.accurest.file

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.regex.Pattern

/**
 * @author Jakub Kubrynski
 */
class ContractFileScanner {

	private static final String MATCH_PREFIX = "glob:"
	private static final Pattern SCENARIO_STEP_FILENAME_PATTERN = Pattern.compile("[0-9]+_.*")
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
			String syntaxAndPattern = MATCH_PREFIX + baseDir.toString() + File.separator + it
			if (SystemUtils.IS_OS_WINDOWS) {
				syntaxAndPattern = syntaxAndPattern.replace("\\", "\\\\")
			}
			fileSystem.getPathMatcher(syntaxAndPattern)
		}) as Set
	}

	ListMultimap<Path, Contract> findContracts() {
		ListMultimap<Path, Contract> result = ArrayListMultimap.create()
		appendRecursively(baseDir, result)
		return result
	}

	private void appendRecursively(File baseDir, ListMultimap<Path, Contract> result) {
		File[] files = baseDir.listFiles()
		if (!files) {
			return;
		}
		files.sort().eachWithIndex { File file, int index ->
			if (!matchesPattern(file, excludeMatchers)) {
				if (isContractFile(file)) {
					Path path = file.toPath()
					Integer order = null
					if (hasScenarioFilenamePattern(path)) {
						order = index
					}
					result.put(file.parentFile.toPath(), new Contract(path, matchesPattern(file, ignoreMatchers), files.size(), order))
				} else {
					appendRecursively(file, result)
				}
			}
		}
	}

	private boolean hasScenarioFilenamePattern(Path path) {
		SCENARIO_STEP_FILENAME_PATTERN.matcher(path.fileName.toString()).matches()
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
