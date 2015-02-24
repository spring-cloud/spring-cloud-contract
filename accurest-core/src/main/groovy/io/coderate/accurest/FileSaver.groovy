package io.coderate.accurest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.util.NamesUtil

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import static io.coderate.accurest.util.NamesUtil.capitalize

@CompileStatic
@Slf4j
class FileSaver {

	File targetDirectory
	TestFramework framework

	FileSaver(File targetDirectory, TestFramework framework) {
		this.targetDirectory = targetDirectory
		this.framework = framework
	}

	void saveClassFile(String fileName, String packageName, byte[] classBytes) {

		Path testBaseDir = Paths.get(targetDirectory.absolutePath, NamesUtil.packageToDirectory(packageName))
		Files.createDirectories(testBaseDir)
		Path classPath = Paths.get(testBaseDir.toString(), capitalize(fileName) + framework.classExtension).toAbsolutePath()
		log.info("Creating new class file [$classPath]")
		Files.write(classPath, classBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

}
