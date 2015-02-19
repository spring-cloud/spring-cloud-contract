package io.codearte.accurest.wiremock

import groovy.io.FileType
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
class RecursiveFilesConverter {

    private final SingleFileConverter singleFileConverter

    RecursiveFilesConverter(SingleFileConverter singleFileConverter) {
        this.singleFileConverter = singleFileConverter
    }

    void processDirectory(File sourceRootDirectory, File targetRootDirectory) {
        sourceRootDirectory.eachFileRecurse(FileType.FILES) { File sourceFile ->
            try {
                if (!singleFileConverter.canHandleFileName(sourceFile.name)) {
                    return
                }

                String convertedContent = singleFileConverter.convertContent(sourceFile.text)

                Path relativePath = Paths.get(sourceRootDirectory.toURI()).relativize(sourceFile.parentFile.toPath())
                Path absoluteTargetPath = targetRootDirectory.toPath().resolve(relativePath)
                Files.createDirectories(absoluteTargetPath)

                File newGroovyFile = new File(absoluteTargetPath.toFile(), singleFileConverter.getOutputFileNameForInputFileName(sourceFile.name))
                log.info("Creating new json [$newGroovyFile.path]")
                newGroovyFile.text = convertedContent
            } catch (Exception e) {
                throw e
            }
        }
    }
}
