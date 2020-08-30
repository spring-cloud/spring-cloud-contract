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

package org.springframework.cloud.contract.verifier.plugin;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.springframework.cloud.contract.stubrunner.ContractProjectUpdater;
import org.springframework.cloud.contract.stubrunner.ScmStubDownloaderBuilder;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.util.StringUtils;

/**
 * For SCM based repositories will copy the generated stubs
 * to the cloned repo with contracts and stubs. Will also
 * commit the changes and push them to origin.
 *
 * NOTE: starting with 2.3.0.RELEASE the <code>customize{}</code> closure previously used for
 * {@link PublishStubsToScmTask} customisation is no longer available. The settings should be applied directly
 * within the <code>publishStubsToScm</code> closure as in the example above.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 2.0.0
 */
class PublishStubsToScmTask extends DefaultTask {

	static final String TASK_NAME = "publishStubsToScm";

	private ContractRepository contractRepository;

	private Property<StubRunnerProperties.StubsMode> contractsMode;

	// FIXME: deleting the stubs after tests breaks incremental builds
	private Property<Boolean> deleteStubsAfterTest;

	private Property<Boolean> failOnNoContracts;

	private MapProperty<String, String> contractsProperties;

	private DirectoryProperty stubsDir;

	@Inject
	PublishStubsToScmTask(ObjectFactory objects) {
		this.contractRepository = objects.newInstance(ContractRepository.class);
		this.contractsMode = objects.property(StubRunnerProperties.StubsMode.class);
		this.deleteStubsAfterTest = objects.property(Boolean.class);
		this.failOnNoContracts = objects.property(Boolean.class);
		this.contractsProperties = objects.mapProperty(String.class, String.class);
		this.stubsDir = objects.directoryProperty();

		this.onlyIf(task -> {
			String contractRepoUrl = contractRepository.repositoryUrl.getOrElse("");
			if (StringUtils.isEmpty(contractRepoUrl) || !ScmStubDownloaderBuilder.isProtocolAccepted(contractRepoUrl)) {
				getLogger().warn("Skipping pushing stubs to scm since your contracts repository URL [{}] doesn't match any of the accepted protocols for SCM stub downloader", contractRepoUrl);
				return false;
			}
			return true;
		});
	}

	@TaskAction
	void publishStubsToScm() {
		String projectName = getProject().getGroup().toString() + ":" + getProject().getName() + ":" + getProject().getVersion().toString();
		getLogger().info("Pushing Stubs to SCM for project [{}]", projectName);
		StubRunnerOptions stubRunnerOptions = createStubRunnerOptions();
		new ContractProjectUpdater(stubRunnerOptions).updateContractProject(projectName, stubsDir.get().getAsFile().toPath());
	}

	@Nested
	ContractRepository getContractRepository() {
		return contractRepository;
	}

	class ContractRepository {
		private Property<String> repositoryUrl;
		private Property<String> username;
		private Property<String> password;
		private Property<Integer> proxyPort;
		private Property<String> proxyHost;

		@Inject
		ContractRepository(ObjectFactory objects) {
			this.repositoryUrl = objects.property(String.class);
			this.username = objects.property(String.class);
			this.password = objects.property(String.class);
			this.proxyHost = objects.property(String.class);
			this.proxyPort = objects.property(Integer.class);
		}

		@Input
		@Optional
		public Property<String> getRepositoryUrl() {
			return repositoryUrl;
		}

		@Input
		@Optional
		public Property<String> getUsername() {
			return username;
		}

		@Input
		@Optional
		public Property<String> getPassword() {
			return password;
		}

		@Input
		@Optional
		public Property<Integer> getProxyPort() {
			return proxyPort;
		}

		@Input
		@Optional
		public Property<String> getProxyHost() {
			return proxyHost;
		}
	}

	@Input
	Property<StubRunnerProperties.StubsMode> getContractsMode() {
		return contractsMode;
	}

	@Input
	Property<Boolean> getDeleteStubsAfterTest() {
		return deleteStubsAfterTest;
	}

	@Input
	Property<Boolean> getFailOnNoContracts() {
		return failOnNoContracts;
	}

	@Input
	MapProperty<String, String> getContractsProperties() {
		return contractsProperties;
	}

	@InputDirectory
	@PathSensitive(PathSensitivity.RELATIVE)
	DirectoryProperty getStubsDir() {
		return stubsDir;
	}

	private StubRunnerOptions createStubRunnerOptions() {
		StubRunnerOptionsBuilder options = new StubRunnerOptionsBuilder()
				.withOptions(StubRunnerOptions.fromSystemProps())
				.withStubRepositoryRoot(contractRepository.repositoryUrl.getOrNull())
				.withStubsMode(contractsMode.get())
				.withUsername(contractRepository.username.getOrNull())
				.withPassword(contractRepository.password.getOrNull())
				.withDeleteStubsAfterTest(deleteStubsAfterTest.get())
				.withProperties(contractsProperties.getOrNull())
				.withFailOnNoStubs(failOnNoContracts.get());
		if (contractRepository.proxyPort.isPresent()) {
			options = options.withProxy(contractRepository.proxyHost.getOrNull(), contractRepository.proxyPort.get());
		}
		return options.build();
	}
}
