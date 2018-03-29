/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.plugin

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.TaskAction

import org.springframework.cloud.contract.stubrunner.ContractProjectUpdater
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions

/**
 * For SCM based repositories will copy the generated stubs
 * to the cloned repo with contracts and stubs. Will also
 * commit the changes and push them to origin.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
@PackageScope
@CompileStatic
class PublishStubsToScmTask extends ConventionTask {
	File stubsOutputDir
	ContractVerifierExtension configProperties
	GradleContractsDownloader downloader

	@TaskAction
	void publishStubsToScm() {
		String projectName = project.group.toString() + ":" + project.name.toString() + ":" + this.project.version.toString()
		project.logger.info("Pushing Stubs to SCM for project [" + projectName + "]")
		StubRunnerOptions options = getDownloader().options(getConfigProperties())
		new ContractProjectUpdater(options).updateContractProject(projectName, getStubsOutputDir().toPath());
	}
}
