/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.converter

import java.nio.file.Path
import java.nio.file.Paths

import groovy.io.FileType
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.util.FileSystemUtils

class RecursiveFilesConverterSpec extends Specification {

	private static
	final Set<Path> EXPECTED_TARGET_FILES = [Paths.get("dslRoot.json"),
											 Paths.get("dir1/1_scenario.json"),
											 Paths.get("dir1/2_scenario.json"),
											 Paths.get("dir1/1_scenario_multiple.json"),
											 Paths.get("dir1/2_scenario_multiple.json"),
											 Paths.get("dir1/dsl1.json"),
											 Paths.get("dir1/dsl1b.json"),
											 Paths.get("dir2/dsl2.json"),
											 Paths.get("dir1/dsl1_list_0.json"),
											 Paths.get("dir1/dsl1_list_1.json"),
											 Paths.get("dir1/shouldHaveIndex1.json"),
											 Paths.get("dir1/shouldHaveIndex2.json")]

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder()

	def "should recursively convert all matching files"() {
		given:
			File originalSourceRootDirectory = new File(this.getClass()
															.getResource("/converter/source").toURI())
			File contractsDslDir = tmpFolder.newFolder("source")
			File stubsOutputDir = tmpFolder.newFolder("target")
			FileSystemUtils
					.copyRecursively(originalSourceRootDirectory, contractsDslDir)
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(stubsOutputDir, contractsDslDir, new ArrayList<>(), ".*", false)
		when:
			recursiveFilesConverter.processFiles()
		then:
			Collection<File> createdFiles = [] as List
			stubsOutputDir.
					eachFileRecurse(FileType.FILES) { createdFiles << it }
			Set<String> relativizedCreatedFiles =
					getRelativePathsForFilesInDirectory(createdFiles, stubsOutputDir)
			relativizedCreatedFiles == EXPECTED_TARGET_FILES
		and:
			createdFiles.each { assert it.text.contains("uuid") }
	}

	def "should recursively convert matching files with exlusions"() {
		given:
			File originalSourceRootDirectory = new File(this.getClass()
															.getResource("/converter/source").toURI())
			File contractsDslDir = tmpFolder.newFolder("source")
			File stubsOutputDir = tmpFolder.newFolder("target")
			List<String> excludedFiles = ["dir1/**"]
			FileSystemUtils
					.copyRecursively(originalSourceRootDirectory, contractsDslDir)
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(stubsOutputDir, contractsDslDir, excludedFiles, ".*", false)
		when:
			recursiveFilesConverter.processFiles()
		then:
			Collection<File> createdFiles = [] as List
			stubsOutputDir.
					eachFileRecurse(FileType.FILES) { createdFiles << it }
			Set<String> relativizedCreatedFiles =
					getRelativePathsForFilesInDirectory(createdFiles, stubsOutputDir)
			[Paths.get("dslRoot.json"), Paths.
					get("dir2/dsl2.json")] as Set == relativizedCreatedFiles as Set
		and:
			createdFiles.each { assert it.text.contains("uuid") }
	}

	def "on failure should break processing and throw meaningful exception"() {
		given:
			File sourceFile = tmpFolder.newFile("test.groovy")
			sourceFile.text = """\
org.springframework.cloud.contract.spec.Contract.make { 
		request { 
			method GET()
			url '/foo' 
		} 
		response { 
			status OK() 
		} 
	}"""
		and:
			StubGenerator stubGenerator = Stub(StubGenerator)
			stubGenerator.canHandleFileName(_) >> { true }
			stubGenerator.convertContents(_, _) >> {
				throw new NullPointerException("Test conversion error")
			}
			stubGenerator
					.generateOutputFileNameForInput(_) >> { String inputFileName -> "${inputFileName}2" }
			File contractsDslDir = tmpFolder.root
			File stubsOutputDir = tmpFolder.root
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(stubsOutputDir, contractsDslDir, new ArrayList<>(), ".*", false, new StubGeneratorProvider([stubGenerator]))
		when:
			recursiveFilesConverter.processFiles()
		then:
			ConversionContractVerifierException e = thrown(ConversionContractVerifierException)
			e.message?.contains(sourceFile.name)
			e.cause?.message == "Test conversion error"
	}

	def "should convert contract into stub using all possible converters"() {
		given:
			File sourceFile = tmpFolder.newFile("test.groovy")
			sourceFile.text = """
			org.springframework.cloud.contract.spec.Contract.make {
				request {
					url "/baz"
					method "GET"
				}
				response {
					status OK()
				}
			}
			"""
		and:
			StubGenerator stubGenerator1 = stubGenerator("foo")
		and:
			StubGenerator stubGenerator2 = stubGenerator("bar")
		and:
			File contractsDslDir = tmpFolder.root
			File stubsOutputDir = tmpFolder.root
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(stubsOutputDir, contractsDslDir, new ArrayList<>(), ".*", false, new StubGeneratorProvider([stubGenerator1, stubGenerator2]))
		when:
			recursiveFilesConverter.processFiles()
		then:
			tmpFolder.root.list().toList().containsAll("foo", "bar")
	}

	def "should not create stub file when generated stub is empty"() {
		given:
			File sourceFile = tmpFolder.newFile("test.groovy")
			sourceFile.text = """
			    org.springframework.cloud.contract.spec.Contract.make {
				    request {
					    url "/baz"
					    method "GET"
				    }
				    response {
					    status OK()
				    }
			    }
			    """
		and:
			StubGenerator stubGenerator = stubGenerator("")
		and:
			File contractsDslDir = tmpFolder.root
			File stubsOutputDir = tmpFolder.newFolder("target")
		and:
			RecursiveFilesConverter recursiveFilesConverter = new RecursiveFilesConverter(stubsOutputDir, contractsDslDir, new ArrayList<>(), ".*", false, new StubGeneratorProvider([stubGenerator]))
		when:
			recursiveFilesConverter.processFiles()
		then:
			stubsOutputDir.list().toList().isEmpty()
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
						(content.convertedContract.first()): stub
				]
			}

			@Override
			String generateOutputFileNameForInput(String inputFileName) {
				return stub
			}
		}
	}
}
