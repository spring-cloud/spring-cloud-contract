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

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.HashMap;

import org.assertj.core.api.BDDAssertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class ContractProjectUpdaterTest extends AbstractGitTest {

	@Rule
	public OutputCaptureRule outputCapture = new OutputCaptureRule();

	File originalProject;

	File project;

	ContractProjectUpdater updater;

	GitRepo gitRepo;

	File origin;

	@Before
	public void setup() throws Exception {
		GitContractsRepo.CACHED_LOCATIONS.clear();
		this.originalProject = new File(
				GitRepoTests.class.getResource("/git_samples/contract-git").toURI());
		TestUtils.prepareLocalRepo();
		this.gitRepo = new GitRepo(this.tmpFolder);
		this.origin = clonedProject(this.tmp.newFolder(), this.originalProject);
		this.project = this.gitRepo.cloneProject(this.originalProject.toURI());
		this.gitRepo.checkout(this.project, "master");
		setOriginOnProjectToTmp(this.origin, this.project, true);
		StubRunnerOptions options = new StubRunnerOptionsBuilder()
				.withStubRepositoryRoot("file://" + this.project.getAbsolutePath() + "/")
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE).build();
		this.updater = new ContractProjectUpdater(options);
	}

	@Test
	public void should_push_changes_to_current_branch() throws Exception {
		File stubs = new File(
				GitRepoTests.class.getResource("/git_samples/sample_stubs").toURI());

		this.updater.updateContractProject("hello-world", stubs.toPath());

		// project, not origin, cause we're making one more clone of the local copy
		try (Git git = openGitProject(this.project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage())
					.isEqualTo("Updating project [hello-world] with stubs");
			// I have no idea but the file gets deleted after pushing
			git.reset().setMode(ResetCommand.ResetType.HARD).call();
		}
		BDDAssertions.then(new File(this.project,
				"META-INF/com.example/hello-world/0.0.2/mappings/someMapping.json"))
				.exists();
		BDDAssertions.then(this.gitRepo.gitFactory.provider).isNull();
		BDDAssertions.then(this.outputCapture.toString())
				.contains("No custom credentials provider will be set");
	}

	@Test
	public void should_push_changes_to_current_branch_using_credentials()
			throws Exception {
		StubRunnerOptions options = new StubRunnerOptionsBuilder()
				.withStubRepositoryRoot("file://" + this.project.getAbsolutePath() + "/")
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
				.withProperties(new HashMap<String, String>() {
					{
						put("git.username", "foo");
						put("git.password", "bar");
					}
				}).build();
		ContractProjectUpdater updater = new ContractProjectUpdater(options);
		File stubs = new File(
				GitRepoTests.class.getResource("/git_samples/sample_stubs").toURI());

		updater.updateContractProject("hello-world", stubs.toPath());

		// project, not origin, cause we're making one more clone of the local copy
		try (Git git = openGitProject(this.project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage())
					.isEqualTo("Updating project [hello-world] with stubs");
			// I have no idea but the file gets deleted after pushing
			git.reset().setMode(ResetCommand.ResetType.HARD).call();
		}
		BDDAssertions.then(new File(this.project,
				"META-INF/com.example/hello-world/0.0.2/mappings/someMapping.json"))
				.exists();
		BDDAssertions.then(this.outputCapture.toString()).contains(
				"Passed username and password - will set a custom credentials provider");
	}

	@Test
	public void should_push_changes_to_current_branch_using_root_credentials()
			throws Exception {
		StubRunnerOptions options = new StubRunnerOptionsBuilder()
				.withStubRepositoryRoot("file://" + this.project.getAbsolutePath() + "/")
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE).withUsername("foo")
				.withPassword("bar").build();
		ContractProjectUpdater updater = new ContractProjectUpdater(options);
		File stubs = new File(
				GitRepoTests.class.getResource("/git_samples/sample_stubs").toURI());

		updater.updateContractProject("hello-world", stubs.toPath());

		// project, not origin, cause we're making one more clone of the local copy
		try (Git git = openGitProject(this.project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage())
					.isEqualTo("Updating project [hello-world] with stubs");
			// I have no idea but the file gets deleted after pushing
			git.reset().setMode(ResetCommand.ResetType.HARD).call();
		}
		BDDAssertions.then(new File(this.project,
				"META-INF/com.example/hello-world/0.0.2/mappings/someMapping.json"))
				.exists();
		BDDAssertions.then(this.outputCapture.toString()).contains(
				"Passed username and password - will set a custom credentials provider");
	}

	@Test
	public void should_not_push_changes_to_current_branch_when_no_changes_were_made()
			throws Exception {
		String initialCommit;
		try (Git git = openGitProject(this.origin)) {
			RevCommit revCommit = git.log().call().iterator().next();
			initialCommit = revCommit.getShortMessage();
		}

		this.updater.updateContractProject("hello-world", this.origin.toPath());

		try (Git git = openGitProject(this.project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage()).isEqualTo(initialCommit);
		}
		BDDAssertions.then(new File(this.project,
				"META-INF/com.example/hello-world/0.0.2/mappings/someMapping.json"))
				.doesNotExist();
	}

}
