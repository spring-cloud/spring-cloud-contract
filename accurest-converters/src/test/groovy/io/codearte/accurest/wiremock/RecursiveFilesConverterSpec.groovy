package io.codearte.accurest.wiremock

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class RecursiveFilesConverterSpec extends Specification {

    private static final Set<String> EXPECTED_TARGET_FILES = ["dslRoot.json", "dir1/dsl1.json", "dir1/dsl1b.json", "dir2/dsl2.json"]

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    def "should recursively convert all matching files"() {
        given:
            def singleFileConverterStub = Stub(SingleFileConverter)
            singleFileConverterStub.canHandleFileName(_) >> { String fileName -> fileName.endsWith(".groovy") }
            singleFileConverterStub.convertContent(_) >> { "converted" }
            singleFileConverterStub.getOutputFileNameForInputFileName(_) >> { String inputFileName -> inputFileName.replaceAll('.groovy', '.json') }
            RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(singleFileConverterStub)
        and:
            File originalSourceRootDirectory = new File(this.getClass().getResource("/converter/source").toURI())
            File sourceRootDirectory = tmpFolder.newFolder("source")
            File targetRootDirectory = tmpFolder.newFolder("target")
            FileUtils.copyDirectory(originalSourceRootDirectory, sourceRootDirectory)
        when:
            recursiveFilesConverter.processDirectory(sourceRootDirectory, targetRootDirectory)
        then:
            Collection<File> createdFiles = FileUtils.listFiles(targetRootDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
            Set<String> relativizedCreatedFiles = getRelativePathsForFilesInDirectory(createdFiles, targetRootDirectory)
            EXPECTED_TARGET_FILES == relativizedCreatedFiles
        and:
            createdFiles.each { it.text == "converted" }
    }

    private static Set<String> getRelativePathsForFilesInDirectory(Collection<File> createdFiles, File targetRootDirectory) {
        Path rootSourcePath = Paths.get(targetRootDirectory.toURI())
        Set<String> relativizedCreatedFiles = createdFiles.collect { File file ->
            rootSourcePath.relativize(Paths.get(file.toURI())).toString()
        }
        relativizedCreatedFiles
    }
}
