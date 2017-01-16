/*
 *  Copyright 2013-2017 the original author or authors.
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
package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;

class CopyContracts {
	private static final Logger log = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass());
	private final MavenProject project;
	private final MavenSession mavenSession;
	private final MavenResourcesFiltering mavenResourcesFiltering;
	private final ContractVerifierConfigProperties config;

	public CopyContracts(MavenProject project, MavenSession mavenSession,
			MavenResourcesFiltering mavenResourcesFiltering,
			ContractVerifierConfigProperties config) {
		this.project = project;
		this.mavenSession = mavenSession;
		this.mavenResourcesFiltering = mavenResourcesFiltering;
		this.config = config;
	}

	public void copy(File contractsDirectory, File outputDirectory)
			throws MojoExecutionException {
		log.info("Copying Spring Cloud Contract Verifier contracts. Only files matching "
				+ "[" + this.config.getIncludedContracts() + "] pattern will end up in "
				+ "the final JAR with stubs.");
		Resource resource = new Resource();
		resource.addInclude(this.config.getIncludedRootFolderAntPattern() + "*.*");
		if (this.config.isExcludeBuildFolders()) {
			resource.addExclude("**/target/**");
			resource.addExclude("**/build/**");
		}
		resource.setDirectory(contractsDirectory.getAbsolutePath());
		MavenResourcesExecution execution = new MavenResourcesExecution();
		execution.setResources(Collections.singletonList(resource));
		execution.setOutputDirectory(new File(outputDirectory, "contracts"));
		execution.setMavenProject(this.project);
		execution.setEncoding("UTF-8");
		execution.setMavenSession(this.mavenSession);
		execution.setInjectProjectBuildFilters(false);
		execution.setOverwrite(true);
		execution.setIncludeEmptyDirs(false);
		execution.setFilterFilenames(false);
		try {
			this.mavenResourcesFiltering.filterResources(execution);
		}
		catch (MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
