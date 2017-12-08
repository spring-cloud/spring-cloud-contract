/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.converter

import groovy.io.FileType
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.util.FileSystemUtils
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class RecursiveFilesConverterSpec extends Specification {

	private static
	final Set<Path> EXPECTED_TARGET_FILES = [Paths.get("dslRoot.json"), Paths.get("dir1/dsl1.json"),
											 Paths.get("dir1/dsl1b.json"), Paths.get("dir2/dsl2.json"),
											 Paths.get("dir1/0_dsl1_list.json"), Paths.get("dir1/1_dsl1_list.json"),
											 Paths.get("dir1/shouldHaveIndex1.json"), Paths.get("dir1/shouldHaveIndex2.json")]

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	def "should recursively convert all matching files"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			File originalSourceRootDirectory = new File(this.getClass().getResource("/converter/source").toURI())
			properties.contractsDslDir = tmpFolder.newFolder("source")
			properties.stubsOutputDir = tmpFolder.newFolder("target")
			FileSystemUtils.copyRecursively(originalSourceRootDirectory, properties.contractsDslDir)
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(properties)
		when:
			recursiveFilesConverter.processFiles()
		then:
			Collection<File> createdFiles = [] as List
			properties.stubsOutputDir.eachFileRecurse(FileType.FILES) {it -> createdFiles << it}
			Set<String> relativizedCreatedFiles = getRelativePathsForFilesInDirectory(createdFiles, properties.stubsOutputDir)
			EXPECTED_TARGET_FILES == relativizedCreatedFiles
		and:
			createdFiles.each { assert it.text.contains("uuid") }
	}

	def "should recursively convert matching files with exlusions"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			File originalSourceRootDirectory = new File(this.getClass().getResource("/converter/source").toURI())
			properties.contractsDslDir = tmpFolder.newFolder("source")
			properties.stubsOutputDir = tmpFolder.newFolder("target")
			properties.excludedFiles = ["dir1/**"]
			FileSystemUtils.copyRecursively(originalSourceRootDirectory, properties.contractsDslDir)
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(properties)
		when:
			recursiveFilesConverter.processFiles()
		then:
			Collection<File> createdFiles = [] as List
			properties.stubsOutputDir.eachFileRecurse(FileType.FILES) {it -> createdFiles << it}
			Set<String> relativizedCreatedFiles = getRelativePathsForFilesInDirectory(createdFiles, properties.stubsOutputDir)
			[Paths.get("dslRoot.json"), Paths.get("dir2/dsl2.json")] as Set == relativizedCreatedFiles as Set
		and:
			createdFiles.each { assert it.text.contains("uuid") }
	}

	def "on failure should break processing and throw meaningful exception"() {
		given:
			def sourceFile = tmpFolder.newFile("test.groovy")
		and:
			def stubGenerator = Stub(StubGenerator)
			stubGenerator.canHandleFileName(_) >> { true }
			stubGenerator.convertContents(_, _) >> { throw new NullPointerException("Test conversion error") }
			stubGenerator.generateOutputFileNameForInput(_) >> { String inputFileName -> "${inputFileName}2" }
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.contractsDslDir = tmpFolder.root
			properties.stubsOutputDir = tmpFolder.root
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(properties, new StubGeneratorProvider([stubGenerator]))
		when:
			recursiveFilesConverter.processFiles()
		then:
			def e = thrown(ConversionContractVerifierException)
			e.message?.contains(sourceFile.name)
			e.cause?.message == "Test conversion error"
	}

	def "should convert contract into stub using all possible converters"() {
		given:
			def sourceFile = tmpFolder.newFile("test.groovy")
			sourceFile.text = """
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					url "/baz"
					method "GET"
				}
				response {
					status 200
				}
			}
			"""
		and:
			StubGenerator stubGenerator1 = stubGenerator("foo")
		and:
			StubGenerator stubGenerator2 = stubGenerator("bar")
		and:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.contractsDslDir = tmpFolder.root
			properties.stubsOutputDir = tmpFolder.root
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(properties, new StubGeneratorProvider([stubGenerator1, stubGenerator2]))
		when:
			recursiveFilesConverter.processFiles()
		then:
			tmpFolder.root.list().toList().containsAll("foo", "bar")
	}

    def "should not create stub file when generated stub is empty"() {
        given:
            def sourceFile = tmpFolder.newFile("test.groovy")
            sourceFile.text = """
			    org.springframework.cloud.contract.spec.Contract.make {
				    request {
					    url "/baz"
					    method "GET"
				    }
				    response {
					    status 200
				    }
			    }
			    """
        and:
            StubGenerator stubGenerator = stubGenerator("")
        and:
            ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
            properties.contractsDslDir = tmpFolder.root
            properties.stubsOutputDir = tmpFolder.newFolder("target")
        and:
            RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(properties, new StubGeneratorProvider([stubGenerator]))
        when:
            recursiveFilesConverter.processFiles()
        then:
            properties.stubsOutputDir.list().toList().isEmpty()
    }

	private static Set<Path> getRelativePathsForFilesInDirectory(Collection<File> createdFiles, File targetRootDirectory) {
		Path rootSourcePath = Paths.get(targetRootDirectory.toURI())
		Set<Path> relativizedCreatedFiles = createdFiles.collect { File file ->
			rootSourcePath.relativize(Paths.get(file.toURI()))
		}
		return relativizedCreatedFiles
	}

	private StubGenerator stubGenerator(String stub) {
		return new StubGenerator() {
			@Override
			boolean canHandleFileName(String fileName) {
				return true
			}

			@Override
			Map<Contract, String> convertContents(String rootName, ContractMetadata content) {
				return [
						(content.convertedContract.first()) : stub
				]
			}

			@Override
			String generateOutputFileNameForInput(String inputFileName) {
				return stub
			}
		}
	}
}
