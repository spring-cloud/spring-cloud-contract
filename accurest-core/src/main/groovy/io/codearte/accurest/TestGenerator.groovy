package io.codearte.accurest
import groovy.transform.PackageScope
import io.codearte.accurest.config.AccurestConfigProperties
import org.apache.commons.io.FilenameUtils
import org.codehaus.plexus.util.DirectoryScanner

import java.util.concurrent.atomic.AtomicInteger

import static io.codearte.accurest.util.NamesUtil.afterLast
/**
 * @author Jakub Kubrynski
 */
class TestGenerator {

	private final AccurestConfigProperties configProperties
	private AtomicInteger counter = new AtomicInteger()
	private SingleTestGenerator generator
	private FileSaver saver
	private DirectoryScanner directoryScanner

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
		this.directoryScanner = new DirectoryScanner()
		directoryScanner.setExcludes(configProperties.getIgnoredFiles() as String[])
		directoryScanner.setBasedir(configProperties.contractsDslDir)
	}

	int generate() {
		generateTestClasses(configProperties.basePackageForTests)
		return counter.get()
	}

	@PackageScope
	void generateTestClasses(final String packageName) {
		directoryScanner.scan()
		directoryScanner.getIncludedDirectories()
				.each { String includedDirectoryRelativePath ->
			processIncludedDirectory(includedDirectoryRelativePath, packageName)

		}
	}

	private void processIncludedDirectory(
			final String includedDirectoryRelativePath, final String packageNameForClass) {
		if (!includedDirectoryRelativePath.isEmpty()) {
			List<File> filesToClass = directoryScanner.includedFiles.
					grep { String includedFile ->
						return normalizePath(includedFile).matches(normalizePath(includedDirectoryRelativePath + File.separator) + "[A-Za-z0-9]*\\.groovy")
					}
			.collect {
				return new File(configProperties.contractsDslDir, it)
			}
			if (filesToClass.size()) {
				def className = afterLast(includedDirectoryRelativePath, File.separator) + configProperties.targetFramework.classNameSuffix
				def classBytes = generator.buildClass(filesToClass, className, packageNameForClass).bytes
				saver.saveClassFile(className, packageNameForClass, classBytes)
				counter.incrementAndGet()
			}
		}
	}

	private String normalizePath(String path) {
		return FilenameUtils.separatorsToUnix(path)
	}
}