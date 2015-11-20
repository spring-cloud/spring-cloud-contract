package io.codearte.accurest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.config.TestFramework

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import static io.codearte.accurest.util.NamesUtil.beforeLast
import static io.codearte.accurest.util.NamesUtil.capitalize
import static io.codearte.accurest.util.NamesUtil.packageToDirectory

@CompileStatic
@Slf4j
class FileSaver {

	File targetDirectory
	TestFramework framework

	FileSaver(File targetDirectory, TestFramework framework) {
		this.targetDirectory = targetDirectory
		this.framework = framework
	}

	void saveClassFile(String fileName, String basePackageClass, String includedDirectoryRelativePath, byte[] classBytes) {

		Path testBaseDir = Paths.get(targetDirectory.absolutePath, packageToDirectory(basePackageClass),
				beforeLast(includedDirectoryRelativePath, File.separator))
		Files.createDirectories(testBaseDir)
		Path classPath = Paths.get(testBaseDir.toString(), capitalize(fileName) + framework.classExtension).toAbsolutePath()
		log.info("Creating new class file [$classPath]")
		Files.write(classPath, classBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

}
