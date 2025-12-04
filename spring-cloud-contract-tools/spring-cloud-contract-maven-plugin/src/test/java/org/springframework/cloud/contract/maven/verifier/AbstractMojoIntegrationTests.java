/*
 * Copyright 2013-present the original author or authors.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.extension.ExtendWith;

@MojoTest
@ExtendWith(MojoExtension.class)
abstract class AbstractMojoIntegrationTests {

	void deleteDirectoryIfExists(Path targetDir) throws Exception {
		if (!Files.exists(targetDir)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(targetDir)) {
			paths.sorted((a, b) -> -a.compareTo(b)).map(Path::toFile).forEach(File::delete);
		}
	}

	void setupBuildPaths(AbstractMojo mojo, Path targetDir) throws Exception {
		Path projectDir = targetDir.getParent();
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = reader.read(Files.newBufferedReader(projectDir.resolve("pom.xml")));
		MavenProject project = new MavenProject(model);
		project.setFile(projectDir.resolve("pom.xml").toFile());
		MojoExtension.setVariableValueToObject(project, "basedir", projectDir.toFile());
		Build build = project.getBuild();
		if (build == null) {
			build = new Build();
		}
		build.setDirectory(targetDir.toString());
		build.setOutputDirectory(targetDir.resolve("classes").toString());
		build.setTestOutputDirectory(targetDir.resolve("test-classes").toString());
		build.setSourceDirectory(projectDir(project).resolve("src/main/java").toString());
		build.setTestSourceDirectory(projectDir(project).resolve("src/test/java").toString());
		project.setBuild(build);
		project.getProperties().setProperty("project.build.directory", targetDir.toString());
		MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		request.setBaseDirectory(project.getBasedir());
		request.setProjectPresent(true);
		MavenSession session = new StubMavenSession(request, project);
		MojoExtension.setVariableValueToObject(mojo, "project", project);
		MavenProject sessionProject = session.getCurrentProject();
		if (sessionProject == null) {
			sessionProject = project;
			session.setCurrentProject(project);
		}
		sessionProject.setBuild(build);
		session.setProjects(List.of(sessionProject));
		MojoExtension.setVariableValueToObject(mojo, "session", session);
	}

	private Path projectDir(MavenProject project) {
		return project.getBasedir().toPath();
	}

	private static final class StubMavenSession extends MavenSession {

		private MavenProject current;

		StubMavenSession(MavenExecutionRequest request, MavenProject project) {
			super(null, request, new DefaultMavenExecutionResult(), List.of(project));
			this.current = project;
		}

		@Override
		public MavenProject getCurrentProject() {
			return this.current;
		}

		@Override
		public void setCurrentProject(MavenProject currentProject) {
			this.current = currentProject;
		}

	}

}
