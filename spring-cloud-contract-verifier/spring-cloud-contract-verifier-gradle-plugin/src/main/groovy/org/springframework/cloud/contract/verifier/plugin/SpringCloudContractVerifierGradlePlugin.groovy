/*
 *  Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 *     <li>generate tests</li>
 *     <li>generate stubs</li>
 * </ul>
 *
 * Also adds the necessary {@code testCompile} dependencies
 *
 * <ul>
 *     <li>WireMock</li>
 *     <li>JSON Assert</li>
 *     <li>AssertJ</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 *
 * @since 1.0.0
 */
class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final String GENERATE_SERVER_TESTS_TASK_NAME = 'generateContractTests'
	private static final String DSL_TO_WIREMOCK_CLIENT_TASK_NAME = 'generateWireMockClientStubs'

	private static final Class IDEA_PLUGIN_CLASS = org.gradle.plugins.ide.idea.IdeaPlugin
	private static final String GROUP_NAME = "Verification"
	private static final String EXTENSION_NAME = 'contractVerifier'

	private Project project

	@Override
	void apply(Project project) {
		this.project = project
		ContractVerifierConfigProperties extension = project.extensions.create(EXTENSION_NAME, ContractVerifierConfigProperties)

		project.check.dependsOn(GENERATE_SERVER_TESTS_TASK_NAME)

		setConfigurationDefaults(extension)
		createGenerateTestsTask(extension)
		createAndConfigureGenerateWireMockClientStubsFromDslTask(extension)
		project.dependencies.add("testCompile", "com.github.tomakehurst:wiremock:2.0.10-beta")
		project.dependencies.add("testCompile", "com.toomuchcoding.jsonassert:jsonassert:0.4.7")
		project.dependencies.add("testCompile", "org.assertj:assertj-core:2.3.0")

		project.afterEvaluate {
			def hasIdea = project.plugins.findPlugin(IDEA_PLUGIN_CLASS)
			if (hasIdea) {
				project.idea {
					module {
						testSourceDirs += extension.generatedTestSourcesDir
						testSourceDirs += extension.contractsDslDir
					}
				}
			}
		}
	}

	private void setConfigurationDefaults(ContractVerifierConfigProperties extension) {
		extension.with {
			generatedTestSourcesDir = project.file("${project.buildDir}/generated-test-sources/contracts")
			contractsDslDir = defaultContractsDir() //TODO: Use sourceset
			basePackageForTests = 'org.springframework.cloud.contract.verifier.tests'
		}
	}

	private File defaultContractsDir() {
		project.file("${project.rootDir}/src/test/resources/contracts")
	}

	private void createGenerateTestsTask(ContractVerifierConfigProperties extension) {
		Task task = project.tasks.create(GENERATE_SERVER_TESTS_TASK_NAME, GenerateServerTestsTask)
		task.description = "Generate server tests from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			contractsDslDir = { extension.contractsDslDir }
			generatedTestSourcesDir = { extension.generatedTestSourcesDir }
			configProperties = { extension }
		}
	}

	private void createAndConfigureGenerateWireMockClientStubsFromDslTask(ContractVerifierConfigProperties extension) {
		Task task = project.tasks.create(DSL_TO_WIREMOCK_CLIENT_TASK_NAME, GenerateWireMockClientStubsFromDslTask)
		task.description = "Generate WireMock client stubs from the contracts"
		task.group = GROUP_NAME
		task.conventionMapping.with {
			contractsDslDir = { extension.contractsDslDir }
			stubsOutputDir = { extension.stubsOutputDir ?: project.file("${project.buildDir}/stubs") }
			configProperties = { extension }
		}
	}
}
