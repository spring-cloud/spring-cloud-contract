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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Marcin Grzejszczak
 */
public abstract class AbstractGitTest {

	@Rule public TemporaryFolder tmp = new TemporaryFolder();
	File tmpFolder;

	@Before
	public void setupTemp() throws IOException {
		this.tmpFolder = this.tmp.newFolder();
	}

	File createNewFile(File project) throws Exception {
		File newFile = new File(project, "newFile");
		newFile.createNewFile();
		try (PrintStream out = new PrintStream(new FileOutputStream(newFile))) {
			out.print("foo");
		}
		try(Git git = openGitProject(project)) {
			git.add().addFilepattern("newFile").call();
		}
		return newFile;
	}

	void setOriginOnProjectToTmp(File origin, File project, boolean push)
			throws GitAPIException, IOException, URISyntaxException {
		try(Git git = openGitProject(project)) {
			RemoteRemoveCommand remove = git.remoteRemove();
			remove.setName("origin");
			remove.call();
			RemoteSetUrlCommand command = git.remoteSetUrl();
			command.setUri(new URIish(origin.toURI().toURL()));
			command.setName("origin");
			command.setPush(push);
			command.call();
			StoredConfig config = git.getRepository().getConfig();
			RemoteConfig originConfig = new RemoteConfig(config, "origin");
			originConfig.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
			originConfig.update(config);
			config.save();
		}
	}

	Git openGitProject(File project) {
		return new GitRepo.JGitFactory().open(project);
	}

	File clonedProject(File baseDir, File projectToClone) throws IOException {
		GitRepo projectRepo = new GitRepo(baseDir);
		projectRepo.cloneProject(projectToClone.toURI());
		return baseDir;
	}
}
