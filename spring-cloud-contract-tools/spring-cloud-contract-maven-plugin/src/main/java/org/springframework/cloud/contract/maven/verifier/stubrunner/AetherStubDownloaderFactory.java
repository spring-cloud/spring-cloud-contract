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
package org.springframework.cloud.contract.maven.verifier.stubrunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloader;

@Named
@Singleton
public class AetherStubDownloaderFactory {
	private final MavenProject project;
	private final RepositorySystem repoSystem;

	@Inject
	public AetherStubDownloaderFactory(RepositorySystem repoSystem,
			MavenProject project) {
		this.repoSystem = repoSystem;
		this.project = project;
	}

	public StubDownloader build(RepositorySystemSession repoSession) {
		return new AetherStubDownloader(this.repoSystem,
				this.project.getRemoteProjectRepositories(), repoSession);
	}
}
