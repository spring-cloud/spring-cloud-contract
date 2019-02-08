/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.plugin


import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

import org.springframework.cloud.contract.stubrunner.ContractProjectUpdater
import org.springframework.cloud.contract.stubrunner.ScmStubDownloaderBuilder
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions

/**
 * For SCM based repositories will copy the generated stubs
 * to the cloned repo with contracts and stubs. Will also
 * commit the changes and push them to origin.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class PublishStubsToScmTask extends ConventionTask {
	File stubsOutputDir
	ContractVerifierExtension configProperties
	GradleContractsDownloader downloader
	private final ExtensionHolderSpec closureHolder = new ClosureHolder()

	@TaskAction
	void publishStubsToScm() {
		ContractVerifierExtension clonedExtension = modifyExtension()
		if (!shouldRun(clonedExtension)) {
			return
		}
		String projectName = project.group.toString() + ":" + project.name.toString() + ":" + this.project.version.toString()
		project.logger.info("Pushing Stubs to SCM for project [" + projectName + "]")
		StubRunnerOptions options = getDownloader().options(clonedExtension)
		new ContractProjectUpdater(options).updateContractProject(projectName, getStubsOutputDir().toPath())
	}

	private ContractVerifierExtension modifyExtension() {
		ContractVerifierExtension clone = getConfigProperties().copy()
		this.closureHolder.extensionClosure.delegate = clone
		this.closureHolder.extensionClosure.call(clone)
		return clone
	}

	private boolean shouldRun(ContractVerifierExtension clonedExtension) {
		String contractRepoUrl = clonedExtension.contractRepository.repositoryUrl ?: ""
		if (!contractRepoUrl || !ScmStubDownloaderBuilder.isProtocolAccepted(contractRepoUrl)) {
			project.logger.warn("Skipping pushing stubs to scm since your contracts repository URL [${contractRepoUrl}] doesn't match any of the accepted protocols for SCM stub downloader")
			return false
		}
		return true
	}

	void customize(@DelegatesTo(ContractVerifierExtension) Closure closure) {
		project.logger.debug("Storing the extension closure")
		this.closureHolder.extensionClosure = closure
	}

	private static class ClosureHolder implements ExtensionHolderSpec {
		Closure extensionClosure = Closure.IDENTITY
	}
}
