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

package org.springframework.cloud.contract.maven.verifier.stubrunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import shaded.org.apache.maven.settings.Settings;

import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Builds {@link StubDownloaderBuilder} for a Maven project.
 *
 * @author Mariusz Smykula
 * @author Eddú Meléndez
 */
@Named
@Singleton
public class AetherStubDownloaderFactory {

	private static final Log log = LogFactory.getLog(AetherStubDownloaderFactory.class);

	private final MavenProject project;

	private final RepositorySystem repoSystem;

	private final Settings settings;

	@Inject
	public AetherStubDownloaderFactory(RepositorySystem repoSystem, MavenProject project,
			Settings settings) {
		this.repoSystem = repoSystem;
		this.project = project;
		this.settings = settings;
	}

	public StubDownloaderBuilder build(final RepositorySystemSession repoSession) {
		return new StubDownloaderBuilder() {
			@Override
			public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
				log.info(
						"Will download contracts using current build's Maven repository setup");
				return new AetherStubDownloader(
						AetherStubDownloaderFactory.this.repoSystem,
						AetherStubDownloaderFactory.this.project
								.getRemoteProjectRepositories(),
						repoSession, AetherStubDownloaderFactory.this.settings);
			}

			@Override
			public Resource resolve(String location, ResourceLoader resourceLoader) {
				return null;
			}
		};
	}

}
