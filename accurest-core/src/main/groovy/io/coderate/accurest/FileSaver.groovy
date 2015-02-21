package io.coderate.accurest

import groovy.transform.CompileStatic
import io.coderate.accurest.config.TestFramework
import io.coderate.accurest.util.NamesUtil

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import static io.coderate.accurest.util.NamesUtil.capitalize

@CompileStatic
class FileSaver {

	File targetDirectory
	TestFramework framework

	FileSaver(File targetDirectory, TestFramework framework) {
		this.targetDirectory = targetDirectory
		this.framework = framework
	}

	void saveClassFile(String fileName, String packageName, byte[] classBytes) {

		def testBaseDir = Paths.get(targetDirectory.absolutePath, NamesUtil.packageToDirectory(packageName))
		Files.createDirectories(testBaseDir)
		def classPath = Paths.get(testBaseDir.toString(), capitalize(fileName) + framework.classNameSuffix + framework.classExtension)
				.toAbsolutePath()
		Files.write(classPath, classBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

}
