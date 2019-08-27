package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties

/**
 * @author Anatoliy Balakirev
 */
@PackageScope
@CompileStatic
class InitContractsTask extends DefaultTask {

	static final String TASK_NAME = 'initContracts'
	protected final GradleContractsDownloader contractDownloader = new GradleContractsDownloader(project, logger)
	@Nested
	Config config

	static class Config {
		// All fields inside `@Nested` one are properly marked as an `@Input` to work with incremental build:
		@Nested
		@Optional
		ContractVerifierExtension.Dependency contractDependency
		@Nested
		@Optional
		ContractVerifierExtension.ContractRepository contractRepository
		@Input
		@Optional
		Property<StubRunnerProperties.StubsMode> contractsMode
		@Input
		Property<Boolean> deleteStubsAfterTest
		@Input
		Property<Boolean> failOnNoContracts
		@Input
		MapProperty<String, String> contractsProperties
		@Input
		@Optional
		Property<String> contractsPath

		@Internal
		Property<String> includedContracts
		@Internal
		Property<String> includedRootFolderAntPattern

		// TODO: Enable it when all caching from `GradleContractsDownloader` is replaced with Gradle's one here:
		// @OutputDirectory
		DirectoryProperty initialisedContractsDirectory
	}

	@TaskAction
	void initContracts() {
		GradleContractsDownloader.DownloadedData downloaded =
				contractDownloader.downloadAndUnpackContractsIfRequired(config.contractDependency,
						config.contractRepository, config.contractsPath.getOrNull(), config.contractsMode.getOrNull(),
						config.deleteStubsAfterTest.get(), config.contractsProperties.get(), config.failOnNoContracts.get())
		if (downloaded) {
			config.includedContracts.set(downloaded.inclusionProperties.includedContracts)
			config.includedRootFolderAntPattern.set(downloaded.inclusionProperties.includedRootFolderAntPattern)
			config.initialisedContractsDirectory.set(downloaded.downloadedContracts)
		}
		logger.info("For project [{}] will use contracts provided in the folder [{}]", project.name,
				config.initialisedContractsDirectory.get())
	}

	static Config fromExtension(ContractVerifierExtension extension, Project project) {
		DirectoryProperty initialisedContractsDirectory = project.objects.directoryProperty()
		initialisedContractsDirectory.set(extension.contractsDslDir)
		return new Config(
				contractDependency: extension.contractDependency,
				contractRepository: extension.contractRepository,
				contractsMode: extension.contractsMode,
				deleteStubsAfterTest: extension.deleteStubsAfterTest,
				failOnNoContracts: extension.failOnNoContracts,
				contractsProperties: extension.contractsProperties,
				contractsPath: extension.contractsPath,
				includedContracts: project.objects.property(String).convention(".*"),
				includedRootFolderAntPattern: project.objects.property(String).convention("**/"),
				initialisedContractsDirectory: initialisedContractsDirectory
		)
	}
}
