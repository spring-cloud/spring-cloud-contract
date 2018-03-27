/*
 * Copyright 2013-2018 the original author or authors.
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
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

/**
 * @author Marcin Grzejszczak
 * taken from: https://github.com/spring-cloud/spring-cloud-release-tools
 */
public class GitRepoTests extends AbstractGitTest {

	File project;
	GitRepo gitRepo;

	@Before
	public void setup() throws IOException, URISyntaxException {
		this.project = new File(GitRepoTests.class.getResource("/git_samples/contract-git").toURI());
		TestUtils.prepareLocalRepo();
		this.gitRepo = new GitRepo(this.tmpFolder);
	}

	@Test
	public void should_clone_the_project_from_a_given_location() throws IOException {
		this.gitRepo.cloneProject(this.project.toURI());

		then(new File(this.tmpFolder, ".git")).exists();
	}

	@Test
	public void should_throw_exception_when_there_is_no_repo() throws IOException, URISyntaxException {
		thenThrownBy(() -> this.gitRepo
				.cloneProject(GitRepoTests.class.getResource("/git_samples/").toURI()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Exception occurred while cloning repo");
	}

	@Test
	public void should_throw_an_exception_when_failed_to_initialize_the_repo() throws IOException {
		thenThrownBy(() ->  new GitRepo(this.tmpFolder, new ExceptionThrowingJGitFactory()).cloneProject(this.project.toURI()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Exception occurred while cloning repo")
				.hasCauseInstanceOf(CustomException.class);
	}

	@Test
	public void should_check_out_a_branch_on_cloned_repo() throws IOException {
		File project = this.gitRepo.cloneProject(this.project.toURI());
		this.gitRepo.checkout(project, "master");

		File pom = new File(this.tmpFolder, "README.adoc");
		then(pom).exists();
	}

	@Test
	public void should_throw_an_exception_when_checking_out_nonexisting_branch() throws IOException {
		File project = this.gitRepo.cloneProject(this.project.toURI());
		try {
			this.gitRepo.checkout(project, "nonExistingBranch");
			fail("should throw an exception");
		} catch (IllegalStateException e) {
			then(e).hasMessageContaining("Ref nonExistingBranch can not be resolved");
		}
	}

	@Test
	public void should_commit_changes() throws Exception {
		File project = this.gitRepo.cloneProject(this.project.toURI());
		createNewFile(project);

		this.gitRepo.commit(project, "some message");

		try(Git git = openGitProject(project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage()).isEqualTo("some message");
		}
	}

	@Test
	public void should_reset_any_changes() throws Exception {
		File project = this.gitRepo.cloneProject(this.project.toURI());
		File file = createNewFile(project);

		this.gitRepo.reset(project);

		then(file).doesNotExist();
	}

	@Test
	public void should_not_commit_empty_changes() throws Exception {
		File project = this.gitRepo.cloneProject(this.project.toURI());
		createNewFile(project);
		this.gitRepo.commit(project, "some message");

		this.gitRepo.commit(project, "empty commit");

		try(Git git = openGitProject(project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage()).isNotEqualTo("empty commit");
		}
	}

	@Test
	public void should_push_changes_to_current_branch() throws Exception {
		File origin = clonedProject(this.tmp.newFolder(), this.project);
		File project = this.gitRepo.cloneProject(this.project.toURI());
		setOriginOnProjectToTmp(origin, project, true);
		createNewFile(project);
		this.gitRepo.commit(project, "some message");

		this.gitRepo.pushCurrentBranch(project);

		try(Git git = openGitProject(origin)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage()).isEqualTo("some message");
		}
	}

	@Test
	public void should_pull_changes_to_current_branch() throws Exception {
		File origin = clonedProject(this.tmp.newFolder(), this.project);
		File project = this.gitRepo.cloneProject(this.project.toURI());
		setOriginOnProjectToTmp(origin, project, false);
		createNewFile(origin);
		this.gitRepo.commit(origin, "some message");

		this.gitRepo.pull(project);

		try(Git git = openGitProject(project)) {
			RevCommit revCommit = git.log().call().iterator().next();
			then(revCommit.getShortMessage()).isEqualTo("some message");
		}
	}
}

class ExceptionThrowingJGitFactory extends GitRepo.JGitFactory {
	@Override CloneCommand getCloneCommandByCloneRepository() {
		throw new CustomException("foo");
	}
}

class CustomException extends RuntimeException {
	public CustomException(String message) {
		super(message);
	}
}