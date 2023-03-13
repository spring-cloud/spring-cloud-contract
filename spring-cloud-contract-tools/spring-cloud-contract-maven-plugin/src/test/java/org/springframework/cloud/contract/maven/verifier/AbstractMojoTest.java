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
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.BDDAssertions;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.util.FileSystemUtils;

public abstract class AbstractMojoTest {

	@Rule
	public MojoRule rule = new MojoRule() {
		@Override
		protected void before() throws Throwable {
		}

		@Override
		protected void after() {
		}
	};

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Rule
	public OutputCaptureRule capture = new OutputCaptureRule();

	File tmpFolder;

	@Before
	public void setupTemp() throws IOException {
		this.tmpFolder = this.tmp.newFolder();
		File projects = new File("src/test/projects");
		FileSystemUtils.copyRecursively(projects, this.tmpFolder);
	}

	protected File getBasedir(String name) {
		return new File(this.tmpFolder, name);
	}

	protected MavenSession prepareMavenSession(File baseDir) throws Exception {
		MavenProject mavenProject = rule.readMavenProject(baseDir);
		return rule.newMavenSession(mavenProject);
	}

	protected void executeMojo(File baseDir, String goal, Xpp3Dom... parameters) throws Exception {
		MavenSession mavenSession = prepareMavenSession(baseDir);
		executeMojo(mavenSession, goal, parameters);
	}

	protected void executeMojo(MavenSession mavenSession, String goal, Xpp3Dom... parameters) throws Exception {
		rule.executeMojo(mavenSession, mavenSession.getCurrentProject(), goal, parameters);
	}

	protected void assertFilesPresent(File file, String name) {
		BDDAssertions.then(new File(file, name)).exists();
	}

	protected void assertFilesNotPresent(File file, String name) {
		BDDAssertions.then(new File(file, name)).doesNotExist();
	}

}
