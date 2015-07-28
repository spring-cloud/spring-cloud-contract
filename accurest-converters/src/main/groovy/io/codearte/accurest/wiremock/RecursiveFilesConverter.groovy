package io.codearte.accurest.wiremock

import groovy.io.FileType
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
@CompileStatic
class RecursiveFilesConverter {

	private final SingleFileConverter singleFileConverter
	private final File sourceRootDirectory
	private final File targetRootDirectory

	RecursiveFilesConverter(SingleFileConverter singleFileConverter, File sourceRootDirectory, File targetRootDirectory) {
		this.singleFileConverter = singleFileConverter
		this.sourceRootDirectory = sourceRootDirectory
		this.targetRootDirectory = targetRootDirectory
	}

	void processFiles() {
		sourceRootDirectory.eachFileRecurse(FileType.FILES) { File sourceFile ->
			try {
				if (!singleFileConverter.canHandleFileName(sourceFile.name)) {
					return
				}
				String convertedContent = singleFileConverter.convertContent(sourceFile.text)
				Path absoluteTargetPath = createAndReturnTargetDirectory(sourceFile)
				File newGroovyFile = createTargetFileWithProperName(absoluteTargetPath, sourceFile)
				newGroovyFile.text = convertedContent
			} catch (Exception e) {
				throw new ConversionAccurestException("Unable to make convertion of ${sourceFile.name}", e)
			}
		}
	}

	private Path createAndReturnTargetDirectory(File sourceFile) {
		Path relativePath = Paths.get(sourceRootDirectory.toURI()).relativize(sourceFile.parentFile.toPath())
		Path absoluteTargetPath = targetRootDirectory.toPath().resolve(relativePath)
		Files.createDirectories(absoluteTargetPath)
		absoluteTargetPath
	}

	private File createTargetFileWithProperName(Path absoluteTargetPath, File sourceFile) {
		File newGroovyFile = new File(absoluteTargetPath.toFile(), singleFileConverter.generateOutputFileNameForInput(sourceFile.name))
		log.info("Creating new json [$newGroovyFile.path]")
		newGroovyFile
	}
}
