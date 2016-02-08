package io.codearte.accurest

import com.google.common.collect.ListMultimap
import groovy.transform.PackageScope
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.file.Contract
import io.codearte.accurest.file.ContractFileScanner
import org.apache.commons.lang3.StringUtils

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import static io.codearte.accurest.util.NamesUtil.afterLast
import static io.codearte.accurest.util.NamesUtil.beforeLast
import static io.codearte.accurest.util.NamesUtil.convertIllegalPackageChars
import static io.codearte.accurest.util.NamesUtil.directoryToPackage

/**
 * @author Jakub Kubrynski
 */
class TestGenerator {

	private final AccurestConfigProperties configProperties
	private final String DEFAULT_CLASS_PREFIX = "Accurest"

	private AtomicInteger counter = new AtomicInteger()
	private SingleTestGenerator generator
	private FileSaver saver
	private ContractFileScanner contractFileScanner

	TestGenerator(AccurestConfigProperties accurestConfigProperties) {
		this(accurestConfigProperties, new SingleTestGenerator(accurestConfigProperties),
				new FileSaver(accurestConfigProperties.generatedTestSourcesDir, accurestConfigProperties.targetFramework))
	}

	TestGenerator(AccurestConfigProperties configProperties, SingleTestGenerator generator, FileSaver saver) {
		this.configProperties = configProperties
		if (configProperties.contractsDslDir == null) {
			throw new AccurestException("Stubs directory not found under " + configProperties.contractsDslDir)
		}
		this.generator = generator
		this.saver = saver
		contractFileScanner = new ContractFileScanner(configProperties.contractsDslDir,
				configProperties.excludedFiles as Set,
				configProperties.ignoredFiles as Set)
	}

	int generate() {
		generateTestClasses(configProperties.basePackageForTests)
		return counter.get()
	}

	@PackageScope
	void generateTestClasses(final String basePackageName) {
		ListMultimap<Path, Contract> contracts = contractFileScanner.findContracts()
		contracts.asMap().entrySet().each {
			Map.Entry<Path, Collection<Contract>> entry -> processIncludedDirectory(relativizeContractPath(entry), entry.getValue(), basePackageName)
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
			final String includedDirectoryRelativePath, Collection<Contract> contracts, final String basePackageNameForClass) {
		if (contracts.size()) {
			def className = afterLast(includedDirectoryRelativePath.toString(), File.separator) + resolveNameSuffix()
			def packageName = buildPackage(basePackageNameForClass, includedDirectoryRelativePath)
			def classBytes = generator.buildClass(contracts, className, packageName).getBytes(StandardCharsets.UTF_8)
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