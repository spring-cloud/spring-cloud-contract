package io.codearte.accurest.wiremock

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class RecursiveFilesConverterSpec extends Specification {

	private static
	final Set<String> EXPECTED_TARGET_FILES = ["dslRoot.json", "dir1/dsl1.json", "dir1/dsl1b.json", "dir2/dsl2.json"]

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	def "should recursively convert all matching files"() {
		given:
			File originalSourceRootDirectory = new File(this.getClass().getResource("/converter/source").toURI())
			File sourceRootDirectory = tmpFolder.newFolder("source")
			File targetRootDirectory = tmpFolder.newFolder("target")
			FileUtils.copyDirectory(originalSourceRootDirectory, sourceRootDirectory)
		and:
			def singleFileConverterStub = Stub(SingleFileConverter)
			singleFileConverterStub.canHandleFileName(_) >> { String fileName -> fileName.endsWith(".groovy") }
			singleFileConverterStub.convertContent(_) >> { "converted" }
			singleFileConverterStub.generateOutputFileNameForInput(_) >> { String inputFileName -> inputFileName.replaceAll('.groovy', '.json') }
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(singleFileConverterStub, sourceRootDirectory, targetRootDirectory)
		when:
			recursiveFilesConverter.processFiles()
		then:
			Collection<File> createdFiles = FileUtils.listFiles(targetRootDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
			Set<String> relativizedCreatedFiles = getRelativePathsForFilesInDirectory(createdFiles, targetRootDirectory)
			EXPECTED_TARGET_FILES == relativizedCreatedFiles
		and:
			createdFiles.each { it.text == "converted" }
	}

	def "on failure should break processing and throw meaningful exception"() {
		given:
			def sourceFile = tmpFolder.newFile()
		and:
			def singleFileConverterStub = Stub(SingleFileConverter)
			singleFileConverterStub.canHandleFileName(_) >> { true }
			singleFileConverterStub.convertContent(_) >> { throw new NullPointerException("Test conversion error") }
			singleFileConverterStub.generateOutputFileNameForInput(_) >> { String inputFileName -> "${inputFileName}2" }
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(singleFileConverterStub, tmpFolder.root, tmpFolder.root)
		when:
			recursiveFilesConverter.processFiles()
		then:
			def e = thrown(ConversionAccurestException)
			e.message?.contains(sourceFile.name)
			e.cause?.message == "Test conversion error"
	}

	private
	static Set<String> getRelativePathsForFilesInDirectory(Collection<File> createdFiles, File targetRootDirectory) {
		Path rootSourcePath = Paths.get(targetRootDirectory.toURI())
		Set<String> relativizedCreatedFiles = createdFiles.collect { File file ->
			rootSourcePath.relativize(Paths.get(file.toURI())).toString()
		}
		relativizedCreatedFiles
	}
}
