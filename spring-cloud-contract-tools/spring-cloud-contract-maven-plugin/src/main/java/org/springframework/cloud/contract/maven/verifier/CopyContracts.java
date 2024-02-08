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

package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;

class CopyContracts {

	private static final Log log = LogFactory.getLog(CopyContracts.class);

	private final MavenProject project;

	private final MavenSession mavenSession;

	private final MavenResourcesFiltering mavenResourcesFiltering;

	private final ContractVerifierConfigProperties config;

	CopyContracts(MavenProject project, MavenSession mavenSession, MavenResourcesFiltering mavenResourcesFiltering,
			ContractVerifierConfigProperties config) {
		this.project = project;
		this.mavenSession = mavenSession;
		this.mavenResourcesFiltering = mavenResourcesFiltering;
		this.config = config;
	}

	public void copy(File contractsDirectory, File outputDirectory) throws MojoExecutionException {
		log.info("Copying Spring Cloud Contract Verifier contracts to [" + outputDirectory + "]"
				+ ". Only files matching [" + this.config.getIncludedContracts() + "] pattern will end up in "
				+ "the final JAR with stubs.");
		Resource resource = new Resource();
		String includedRootFolderAntPattern = this.config.getIncludedRootFolderAntPattern() + "*.*";
		String slashSeparatedGroupIdAntPattern = slashSeparatedGroupIdAntPattern(includedRootFolderAntPattern);
		String dotSeparatedGroupIdAntPattern = dotSeparatedGroupIdAntPattern(includedRootFolderAntPattern);
		// by default group id is slash separated...
		resource.addInclude(slashSeparatedGroupIdAntPattern);
		if (!slashSeparatedGroupIdAntPattern.equals(dotSeparatedGroupIdAntPattern)) {
			// ...we also want to allow dot separation
			resource.addInclude(dotSeparatedGroupIdAntPattern);
		}
		if (this.config.isExcludeBuildFolders()) {
			resource.addExclude("**/target/**");
			resource.addExclude("**/.mvn/**");
			resource.addExclude("**/build/**");
			resource.addExclude("**/.gradle/**");
		}
		resource.setDirectory(contractsDirectory.getAbsolutePath());
		MavenResourcesExecution execution = new MavenResourcesExecution();
		execution.setResources(Collections.singletonList(resource));
		execution.setOutputDirectory(outputDirectory);
		execution.setMavenProject(this.project);
		execution.setEncoding("UTF-8");
		execution.setMavenSession(this.mavenSession);
		execution.setInjectProjectBuildFilters(false);
		execution.setOverwrite(true);
		execution.setIncludeEmptyDirs(false);
		execution.setFilterFilenames(false);
		execution.setFilters(Collections.emptyList());
		try {
			this.mavenResourcesFiltering.filterResources(execution);
		}
		catch (MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private String slashSeparatedGroupIdAntPattern(String includedRootFolderAntPattern) {
		if (includedRootFolderAntPattern.contains(slashSeparatedGroupId())) {
			return includedRootFolderAntPattern;
		}
		else if (includedRootFolderAntPattern.contains(dotSeparatedGroupId())) {
			return includedRootFolderAntPattern.replace(dotSeparatedGroupId(), slashSeparatedGroupId());
		}
		return includedRootFolderAntPattern;
	}

	private String dotSeparatedGroupIdAntPattern(String includedRootFolderAntPattern) {
		if (includedRootFolderAntPattern.contains(dotSeparatedGroupId())) {
			return includedRootFolderAntPattern;
		}
		else if (includedRootFolderAntPattern.contains(slashSeparatedGroupId())) {
			return includedRootFolderAntPattern.replace(slashSeparatedGroupId(), dotSeparatedGroupId());
		}
		return includedRootFolderAntPattern;
	}

	private String slashSeparatedGroupId() {
		return this.project.getGroupId().replace(".", File.separator);
	}

	private String dotSeparatedGroupId() {
		return this.project.getGroupId();
	}

}
