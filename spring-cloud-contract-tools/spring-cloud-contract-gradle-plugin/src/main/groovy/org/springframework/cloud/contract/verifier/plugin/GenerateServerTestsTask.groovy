/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.springframework.cloud.contract.spec.ContractVerifierException
import org.springframework.cloud.contract.verifier.TestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.config.TestFramework
import org.springframework.cloud.contract.verifier.config.TestLanguage
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * Task used to generate server side tests
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Tim Ysewyn
 * @since 1.0.0
 */
@CompileStatic
class GenerateServerTestsTask extends DefaultTask {
	static final String TASK_NAME = 'generateContractTests'
	@Nested
	Config config

	static class Config {
		@InputDirectory
		Provider<Directory> contractsDslDir
		@Input
		@Optional
		Provider<String> nameSuffixForTests
		@Input
		@Optional
		Provider<String> basePackageForTests
		@Input
		@Optional
		Provider<String> baseClassForTests
		@Input
		@Optional
		Provider<String> packageWithBaseClasses
		@Input
		ListProperty<String> excludedFiles
		@Input
		ListProperty<String> ignoredFiles
		@Input
		ListProperty<String> includedFiles
		@Input
		ListProperty<String> imports
		@Input
		ListProperty<String> staticImports
		@Input
		Provider<TestMode> testMode
		@Input
		Provider<TestFramework> testFramework
		@Input
		Provider<TestLanguage> testLanguage
		@Input
		MapProperty<String, String> baseClassMappings
		@Input
		Provider<Boolean> assertJsonSize
		@Input
		Provider<Boolean> failOnInProgress

		@OutputDirectory
		DirectoryProperty generatedTestSourcesDir
		@OutputDirectory
		DirectoryProperty generatedTestResourcesDir
	}

	@TaskAction
	void generate() {
		File generatedTestSources = config.generatedTestSourcesDir.get().asFile
		File generatedTestResources = config.generatedTestResourcesDir.get().asFile
		logger.info("Generated test sources dir [${generatedTestSources}]")
		logger.info("Generated test resources dir [${generatedTestResources}]")
		File contractsDslDir = config.contractsDslDir.get().asFile
		String includedContracts = ".*"
		project.logger.info("Spring Cloud Contract Verifier Plugin: Invoking test sources generation")
		project.logger.info("Contracts are unpacked to [${contractsDslDir}]")
		project.logger.info("Included contracts are [${includedContracts}]")
		project.logger.info("Tests will be generated in ${config.testLanguage.get()}")
		try {
			List<String> excludedFiles = config.excludedFiles.get()
			List<String> ignoredFiles = config.ignoredFiles.get()
			List<String> includedFiles = config.includedFiles.get()
			String[] imports = config.imports.get().toArray(new String[0])
			String[] staticImports = config.staticImports.get().toArray(new String[0])
			TestGenerator generator = new TestGenerator(new ContractVerifierConfigProperties(
					includedContracts: includedContracts,
					contractsDslDir: contractsDslDir,
					nameSuffixForTests: config.nameSuffixForTests.getOrNull(),
					generatedTestSourcesDir: generatedTestSources,
					generatedTestResourcesDir: generatedTestResources,
					basePackageForTests: config.basePackageForTests.getOrNull(),
					baseClassForTests: config.baseClassForTests.getOrNull(),
					packageWithBaseClasses: config.packageWithBaseClasses.getOrNull(),
					excludedFiles: excludedFiles,
					ignoredFiles: ignoredFiles,
					includedFiles: includedFiles,
					imports: imports,
					staticImports: staticImports,
					testMode: config.testMode.get(),
					testFramework: config.testFramework.get(),
					testLanguage: config.testLanguage.get(),
					baseClassMappings: config.baseClassMappings.get(),
					assertJsonSize: config.assertJsonSize.get(),
					failOnInProgress: config.failOnInProgress.get()
			))
			int generatedClasses = generator.generate()
			project.logger.info("Generated {} test classes", generatedClasses)
		}
		catch (ContractVerifierException e) {
			throw new GradleException("Spring Cloud Contract Verifier Plugin exception: ${e.message}", e)
		}
	}

	static Config fromExtension(ContractVerifierExtension extension, TaskProvider<ContractsCopyTask> copyContractsTask) {
		return new Config(
				contractsDslDir: copyContractsTask.flatMap { it.config.copiedContractsFolder },
				nameSuffixForTests: extension.nameSuffixForTests,
				basePackageForTests: extension.basePackageForTests,
				baseClassForTests: extension.baseClassForTests,
				packageWithBaseClasses: extension.packageWithBaseClasses,
				excludedFiles: extension.excludedFiles,
				ignoredFiles: extension.ignoredFiles,
				includedFiles: extension.includedFiles,
				imports: extension.imports,
				staticImports: extension.staticImports,
				testMode: extension.testMode,
				testFramework: extension.testFramework,
				testLanguage: extension.testLanguage,
				baseClassMappings: extension.baseClassMappings.getBaseClassMappings(),
				assertJsonSize: extension.assertJsonSize,
				failOnInProgress: extension.failOnInProgress,

				generatedTestSourcesDir: extension.generatedTestSourcesDir,
				generatedTestResourcesDir: extension.generatedTestResourcesDir,
		)
	}
}
