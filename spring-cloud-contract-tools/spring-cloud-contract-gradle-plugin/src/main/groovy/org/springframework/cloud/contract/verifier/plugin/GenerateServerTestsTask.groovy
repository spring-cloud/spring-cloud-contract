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

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
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
import org.springframework.cloud.contract.verifier.config.TestMode

/**
 * Task used to generate server side tests
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @since 1.0.0
 */
@CompileStatic
class GenerateServerTestsTask extends DefaultTask {
	static final String TASK_NAME = 'generateContractTests'
	@Nested
	Config config

	@CompileStatic
	@Builder
	static class Config {

		final Provider<Directory> contractsDslDir
		final Provider<String> nameSuffixForTests
		final Provider<String> basePackageForTests
		final Provider<String> baseClassForTests
		final Provider<String> packageWithBaseClasses
		final ListProperty<String> excludedFiles
		final ListProperty<String> ignoredFiles
		final ListProperty<String> includedFiles
		final ListProperty<String> imports
		final ListProperty<String> staticImports
		final Provider<TestMode> testMode
		final Provider<TestFramework> testFramework
		final MapProperty<String, String> baseClassMappings
		final Provider<Boolean> assertJsonSize
		final Provider<Boolean> failOnInProgress
		final DirectoryProperty generatedTestSourcesDir
		final DirectoryProperty generatedTestResourcesDir

		Config(Provider<Directory> contractsDslDir, Provider<String> nameSuffixForTests, Provider<String> basePackageForTests, Provider<String> baseClassForTests, Provider<String> packageWithBaseClasses, ListProperty<String> excludedFiles, ListProperty<String> ignoredFiles, ListProperty<String> includedFiles, ListProperty<String> imports, ListProperty<String> staticImports, Provider<TestMode> testMode, Provider<TestFramework> testFramework, MapProperty<String, String> baseClassMappings, Provider<Boolean> assertJsonSize, Provider<Boolean> failOnInProgress, DirectoryProperty generatedTestSourcesDir, DirectoryProperty generatedTestResourcesDir) {
			this.contractsDslDir = contractsDslDir
			this.nameSuffixForTests = nameSuffixForTests
			this.basePackageForTests = basePackageForTests
			this.baseClassForTests = baseClassForTests
			this.packageWithBaseClasses = packageWithBaseClasses
			this.excludedFiles = excludedFiles
			this.ignoredFiles = ignoredFiles
			this.includedFiles = includedFiles
			this.imports = imports
			this.staticImports = staticImports
			this.testMode = testMode
			this.testFramework = testFramework
			this.baseClassMappings = baseClassMappings
			this.assertJsonSize = assertJsonSize
			this.failOnInProgress = failOnInProgress
			this.generatedTestSourcesDir = generatedTestSourcesDir
			this.generatedTestResourcesDir = generatedTestResourcesDir
		}

		@InputDirectory
		Provider<Directory> getContractsDslDir() {
			return contractsDslDir
		}

		@Input
		@Optional
		Provider<String> getNameSuffixForTests() {
			return nameSuffixForTests
		}

		@Input
		@Optional
		Provider<String> getBasePackageForTests() {
			return basePackageForTests
		}

		@Input
		@Optional
		Provider<String> getBaseClassForTests() {
			return baseClassForTests
		}

		@Input
		@Optional
		Provider<String> getPackageWithBaseClasses() {
			return packageWithBaseClasses
		}

		@Input
		ListProperty<String> getExcludedFiles() {
			return excludedFiles
		}

		@Input
		ListProperty<String> getIgnoredFiles() {
			return ignoredFiles
		}

		@Input
		ListProperty<String> getIncludedFiles() {
			return includedFiles
		}

		@Input
		ListProperty<String> getImports() {
			return imports
		}

		@Input
		ListProperty<String> getStaticImports() {
			return staticImports
		}

		@Input
		Provider<TestMode> getTestMode() {
			return testMode
		}

		@Input
		Provider<TestFramework> getTestFramework() {
			return testFramework
		}

		@Input
		MapProperty<String, String> getBaseClassMappings() {
			return baseClassMappings
		}

		@Input
		Provider<Boolean> getAssertJsonSize() {
			return assertJsonSize
		}

		@Input
		Provider<Boolean> getFailOnInProgress() {
			return failOnInProgress
		}

		@OutputDirectory
		DirectoryProperty getGeneratedTestSourcesDir() {
			return generatedTestSourcesDir
		}

		@OutputDirectory
		DirectoryProperty getGeneratedTestResourcesDir() {
			return generatedTestResourcesDir
		}
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
				copyContractsTask.flatMap { it.config.copiedContractsFolder },
				extension.nameSuffixForTests,
				extension.basePackageForTests,
				extension.baseClassForTests,
				extension.packageWithBaseClasses,
				extension.excludedFiles,
				extension.ignoredFiles,
				extension.includedFiles,
				extension.imports,
				extension.staticImports,
				extension.testMode,
				extension.testFramework,
				extension.baseClassMappings.getBaseClassMappings(),
				extension.assertJsonSize,
				extension.failOnInProgress,
				extension.generatedTestSourcesDir,
				extension.generatedTestResourcesDir)
	}
}
