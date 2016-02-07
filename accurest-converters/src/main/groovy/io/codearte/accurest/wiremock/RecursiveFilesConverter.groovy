package io.codearte.accurest.wiremock

import com.google.common.collect.ListMultimap
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.file.Contract
import io.codearte.accurest.file.ContractFileScanner

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
@CompileStatic
class RecursiveFilesConverter {

	private final SingleFileConverter singleFileConverter
	private final AccurestConfigProperties properties

	RecursiveFilesConverter(SingleFileConverter singleFileConverter, AccurestConfigProperties properties) {
		this.properties = properties
		this.singleFileConverter = singleFileConverter
	}

	void processFiles() {
		ContractFileScanner scanner = new ContractFileScanner(properties.contractsDslDir, properties.excludedFiles as Set, [] as Set)
		ListMultimap<Path, Contract> contracts = scanner.findContracts()
		contracts.asMap().entrySet().each { entry ->
			entry.value.each { Contract contract ->
				File sourceFile = contract.path.toFile()
				try {
					if (!singleFileConverter.canHandleFileName(sourceFile.name)) {
						return
					}
					String convertedContent = singleFileConverter.convertContent(entry.key.last().toString(), contract)
					Path absoluteTargetPath = createAndReturnTargetDirectory(sourceFile)
					File newGroovyFile = createTargetFileWithProperName(absoluteTargetPath, sourceFile)
					newGroovyFile.setText(convertedContent, StandardCharsets.UTF_8.toString())
				} catch (Exception e) {
					throw new ConversionAccurestException("Unable to make convertion of ${sourceFile.name}", e)
				}
			}
		}
	}

	private Path createAndReturnTargetDirectory(File sourceFile) {
		Path relativePath = Paths.get(properties.contractsDslDir.toURI()).relativize(sourceFile.parentFile.toPath())
		Path absoluteTargetPath = properties.stubsOutputDir.toPath().resolve(relativePath)
		Files.createDirectories(absoluteTargetPath)
		absoluteTargetPath
	}

	private File createTargetFileWithProperName(Path absoluteTargetPath, File sourceFile) {
		File newGroovyFile = new File(absoluteTargetPath.toFile(), singleFileConverter.generateOutputFileNameForInput(sourceFile.name))
		log.info("Creating new json [$newGroovyFile.path]")
		newGroovyFile
	}
}
