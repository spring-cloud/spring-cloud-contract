/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.verifier.plugin

import spock.lang.Specification

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestMode

class ContractVerifierExtensionSpec extends Specification {
	def "should make a copy of the object"() {
		given:
			ContractVerifierExtension original = new ContractVerifierExtension(
					testFramework: TestFramework.JUNIT5,
					testMode: TestMode.EXPLICIT,
					basePackageForTests: "foo1",
					baseClassForTests: "foo2",
					nameSuffixForTests: "foo3",
					ruleClassForTests: "foo4",
					excludedFiles: ["foo5"],
					includedFiles: ["foo6"],
					ignoredFiles: ["foo7"],
					imports: ["foo8"],
					staticImports: ["foo9"],
					contractsDslDir: new File("foo10"),
					generatedTestSourcesDir: new File("foo11"),
					generatedTestResourcesDir: new File("foo12"),
					stubsOutputDir: new File("foo13"),
					stubsSuffix: 'foo14',
					assertJsonSize: false,
					contractRepository: new ContractVerifierExtension.ContractRepository(
							repositoryUrl: "foo15",
							username: "foo16",
							password: "foo17",
							proxyPort: 18,
							proxyHost: "foo19",
							cacheDownloadedContracts: false
					),
					contractDependency: new ContractVerifierExtension.Dependency(
							groupId: "foo20",
							artifactId: "foo21",
							classifier: "foo22",
							version: "foo23",
							stringNotation: "foo24"
					),
					contractsPath: "foo25",
					contractsMode: StubRunnerProperties.StubsMode.CLASSPATH,
					packageWithBaseClasses: "foo26",
					baseClassMappings: [foo27: "foo28"],
					excludeBuildFolders: false,
					deleteStubsAfterTest: false,
					convertToYaml: false,
					contractsProperties: [foo29: "foo30"]
			)
		when:
			ContractVerifierExtension copy = original.copy()
			original.with {
				testFramework = TestFramework.CUSTOM
				testMode = TestMode.MOCKMVC
				basePackageForTests = "bar1"
				baseClassForTests = "bar2"
				nameSuffixForTests = "bar3"
				ruleClassForTests = "bar4"
				excludedFiles = ["bar5"]
				includedFiles = ["bar6"]
				ignoredFiles = ["bar7"]
				imports = ["bar8"]
				staticImports = ["bar9"]
				contractsDslDir = new File("bar10")
				generatedTestSourcesDir = new File("bar11")
				generatedTestResourcesDir = new File("bar12")
				stubsOutputDir = new File("bar13")
				stubsSuffix = 'bar14'
				assertJsonSize = true
				contractRepository.with {
					repositoryUrl = "bar15"
					username = "bar16"
					password = "bar17"
					proxyPort = 28
					proxyHost = "bar19"
					cacheDownloadedContracts = true
				}
				contractDependency.with {
					groupId = "bar20"
					artifactId = "bar21"
					classifier = "bar22"
					version = "bar23"
					stringNotation = "bar24"
				}
				contractsPath = "bar25"
				contractsMode = StubRunnerProperties.StubsMode.REMOTE
				packageWithBaseClasses = "bar26"
				baseClassMappings = [bar27: "bar28"]
				excludeBuildFolders = true
				deleteStubsAfterTest = true
				convertToYaml = true
				contractsProperties = [bar29: "bar30"]
			}
		then:
			copy.testFramework == TestFramework.JUNIT5
			copy.testMode == TestMode.EXPLICIT
			copy.basePackageForTests == "foo1"
			copy.baseClassForTests == "foo2"
			copy.nameSuffixForTests == "foo3"
			copy.ruleClassForTests == "foo4"
			copy.excludedFiles == ["foo5"]
			copy.includedFiles == ["foo6"]
			copy.ignoredFiles == ["foo7"]
			copy.imports == ["foo8"]
			copy.staticImports == ["foo9"]
			copy.contractsDslDir == new File("foo10")
			copy.generatedTestSourcesDir == new File("foo11")
			copy.generatedTestResourcesDir == new File("foo12")
			copy.stubsOutputDir == new File("foo13")
			copy.stubsSuffix == 'foo14'
			copy.assertJsonSize == false
			copy.contractRepository.repositoryUrl == "foo15"
			copy.contractRepository.username == "foo16"
			copy.contractRepository.password == "foo17"
			copy.contractRepository.proxyPort == 18
			copy.contractRepository.proxyHost == "foo19"
			copy.contractRepository.cacheDownloadedContracts == false
			copy.contractDependency.groupId == "foo20"
			copy.contractDependency.artifactId == "foo21"
			copy.contractDependency.classifier == "foo22"
			copy.contractDependency.version == "foo23"
			copy.contractDependency.stringNotation == "foo24"
			copy.contractsPath == "foo25"
			copy.contractsMode == StubRunnerProperties.StubsMode.CLASSPATH
			copy.packageWithBaseClasses == "foo26"
			copy.baseClassMappings == [foo27: "foo28"]
			copy.excludeBuildFolders == false
			copy.deleteStubsAfterTest == false
			copy.convertToYaml == false
			copy.contractsProperties == [foo29: "foo30"]
	}
}
