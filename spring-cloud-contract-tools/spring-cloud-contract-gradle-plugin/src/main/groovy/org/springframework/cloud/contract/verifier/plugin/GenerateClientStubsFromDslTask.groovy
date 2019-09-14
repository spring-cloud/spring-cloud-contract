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
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverter

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
/**
 * Generates stubs from the contracts.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @since 2.0.0
 */
@CompileStatic
class GenerateClientStubsFromDslTask extends DefaultTask {

	static final String TASK_NAME = 'generateClientStubs'
	private static final String DEFAULT_MAPPINGS_FOLDER = 'mappings'
	@Nested
	Config config

	static class Config {
		@InputDirectory
		Provider<Directory> contractsDslDir
		@Input
		ListProperty<String> excludedFiles
		@Input
		Provider<Boolean> excludeBuildFolders

		@OutputDirectory
		Provider<Directory> stubsOutputDir
	}

	@TaskAction
	void generate() {
		File output = config.stubsOutputDir.get().asFile
		logger.info("Stubs output dir [${output}")
		logger.info("Spring Cloud Contract Verifier Plugin: Invoking DSL to client stubs conversion")
		logger.info("Contracts dir is [${config.contractsDslDir.get().asFile}] output stubs dir is [${output}]")
		List<String> excludedFiles = config.excludedFiles.get()
		RecursiveFilesConverter converter = new RecursiveFilesConverter(output,
				config.contractsDslDir.get().asFile, excludedFiles, ".*", config.excludeBuildFolders.get())
		converter.processFiles()
	}

	static Config fromExtension(ContractVerifierExtension extension, TaskProvider<ContractsCopyTask> copyContracts,
								String root, Project project) {
		return new Config(
				contractsDslDir: copyContracts.flatMap { it.config.copiedContractsFolder },
				excludedFiles: extension.excludedFiles,
				excludeBuildFolders: extension.excludeBuildFolders,

				stubsOutputDir: createTaskOutput(root, extension.stubsOutputDir, project)
		)
	}

	private static DirectoryProperty createTaskOutput(String root, DirectoryProperty stubsOutputDir, Project project) {
		Provider<Directory> provider = stubsOutputDir.flatMap {
			Directory dir = it
			File output = new File(dir.asFile, "${root}/${DEFAULT_MAPPINGS_FOLDER}")

			DirectoryProperty property = project.objects.directoryProperty();
			property.set(output)
			return property
		}
		DirectoryProperty property = project.objects.directoryProperty();
		property.set(provider)
		return property
	}
}
