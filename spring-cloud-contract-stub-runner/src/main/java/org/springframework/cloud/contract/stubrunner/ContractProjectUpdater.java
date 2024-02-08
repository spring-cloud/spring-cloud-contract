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
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Updates the project containing contracts.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public class ContractProjectUpdater {

	private static final Log log = LogFactory.getLog(ContractProjectUpdater.class);

	private static final int DEFAULT_ATTEMPTS_NO = 10;

	private static final long DEFAULT_WAIT_BETWEEN_ATTEMPTS = 1000;

	// TODO: Add this to the documentation
	private static final String DEFAULT_COMMIT_MESSAGE = "Updating project [$project] with stubs";

	private static final String GIT_ATTEMPTS_NO_PROP = "git.no-of-attempts";

	private static final String GIT_WAIT_BETWEEN_ATTEMPTS = "git.wait-between-attempts";

	private static final String GIT_COMMIT_MESSAGE = "git.commit-message";

	private final StubRunnerOptions stubRunnerOptions;

	private final GitContractsRepo gitContractsRepo;

	public ContractProjectUpdater(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.gitContractsRepo = new GitContractsRepo(stubRunnerOptions);
	}

	/**
	 * Merges the folder with stubs with the project containing contracts.
	 * @param projectName name of the project
	 * @param rootStubsFolder root folder of the stubs
	 */
	public void updateContractProject(String projectName, Path rootStubsFolder) {
		File clonedRepo = this.gitContractsRepo.clonedRepo(this.stubRunnerOptions.stubRepositoryRoot);
		GitStubDownloaderProperties properties = new GitStubDownloaderProperties(
				this.stubRunnerOptions.stubRepositoryRoot, this.stubRunnerOptions);
		copyStubs(projectName, rootStubsFolder, clonedRepo);
		GitRepo gitRepo = new GitRepo(clonedRepo, properties);
		String msg = StubRunnerPropertyUtils.getProperty(this.stubRunnerOptions.getProperties(), GIT_COMMIT_MESSAGE);
		GitRepo.CommitResult commit = gitRepo.commit(clonedRepo, commitMessage(projectName, msg));
		if (commit == GitRepo.CommitResult.EMPTY) {
			log.info("There were no changes to commit. Won't push the changes");
			return;
		}
		String attempts = StubRunnerPropertyUtils.getProperty(this.stubRunnerOptions.getProperties(),
				GIT_ATTEMPTS_NO_PROP);
		int intAttempts = StringUtils.hasText(attempts) ? Integer.parseInt(attempts) : DEFAULT_ATTEMPTS_NO;
		String wait = StubRunnerPropertyUtils.getProperty(this.stubRunnerOptions.getProperties(),
				GIT_WAIT_BETWEEN_ATTEMPTS);
		long longWait = StringUtils.hasText(wait) ? Long.parseLong(wait) : DEFAULT_WAIT_BETWEEN_ATTEMPTS;
		tryToPushCurrentBranch(clonedRepo, gitRepo, intAttempts, longWait);
	}

	private void tryToPushCurrentBranch(File clonedRepo, GitRepo gitRepo, int intAttempts, long longWait) {
		int currentAttempt = 0;
		while (currentAttempt < intAttempts) {
			log.info("Trying to push changes, attempt " + (currentAttempt + 1) + "/" + intAttempts);
			gitRepo.pull(clonedRepo);
			log.info("Successfully pulled changes from remote for project with contract and stubs");
			try {
				gitRepo.pushCurrentBranch(clonedRepo);
				log.info("Successfully pushed changes with current stubs");
				break;
			}
			catch (IllegalStateException e) {
				// empty
				log.error("Exception occurred while trying to push the changes", e);
				currentAttempt++;
				if (currentAttempt == intAttempts) {
					throw new IllegalStateException(
							"Failed to push changes to the project with contracts and stubs. Exceeded number of retries ["
									+ intAttempts + "]");
				}
				try {
					Thread.sleep(longWait);
				}
				catch (InterruptedException e1) {
					throw new IllegalStateException(e1);
				}
			}
		}
	}

	private String commitMessage(String projectName, String msg) {
		return StringUtils.hasText(msg) ? replaceProject(projectName, msg)
				: replaceProject(projectName, DEFAULT_COMMIT_MESSAGE);
	}

	private String replaceProject(String projectName, String msg) {
		return msg.replace("$project", projectName);
	}

	private void copyStubs(String projectName, Path rootStubsFolder, File clonedRepo) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Copying stubs from [" + rootStubsFolder.toString() + "] to the cloned repo ["
						+ clonedRepo.getAbsolutePath() + "] for project [" + projectName + "]");
			}
			Files.walkFileTree(rootStubsFolder, new DirectoryCopyingVisitor(rootStubsFolder, clonedRepo.toPath()));
			if (log.isDebugEnabled()) {
				log.debug("Successfully copied stubs to the cloned repo for project [" + projectName + "]");
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}

class DirectoryCopyingVisitor extends SimpleFileVisitor<Path> {

	private static final List<String> FOLDERS_TO_DELETE = Arrays.asList("contracts", "mappings");

	private static final Log log = LogFactory.getLog(DirectoryCopyingVisitor.class);

	private final Path from;

	private final Path to;

	DirectoryCopyingVisitor(Path from, Path to) {
		this.from = from;
		this.to = to;
		if (log.isDebugEnabled()) {
			log.debug("Will copy from [" + from.toString() + "] to [" + to.toString() + "]");
		}
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path relativePath = this.from.relativize(dir);
		if (".git".equals(relativePath.toString())) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		Path targetPath = this.to.resolve(relativePath);
		if (!Files.exists(targetPath)) {
			if (log.isDebugEnabled()) {
				log.debug("Created a folder [" + targetPath.toString() + "]");
			}
			Files.createDirectory(targetPath);
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("Folder [" + targetPath.toString() + "] already exists");
			}
			if (FOLDERS_TO_DELETE.contains(targetPath.toFile().getName())) {
				if (log.isDebugEnabled()) {
					log.debug("Will remove the folder [" + targetPath.toString() + "]");
				}
				deleteRecursively(targetPath);
				Files.createDirectory(targetPath);
				if (log.isDebugEnabled()) {
					log.debug("Recreated folder [" + targetPath.toString() + "]");
				}
			}
		}
		return FileVisitResult.CONTINUE;
	}

	private boolean deleteRecursively(@Nullable Path root) throws IOException {
		if (root == null) {
			return false;
		}
		if (!Files.exists(root)) {
			return false;
		}
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// a hack for Windows not to fail when directory is removed
				// related to
				// https://github.com/spring-cloud/spring-cloud-sleuth/issues/834
				if (exc == null) {
					int maxTries = 5;
					int count = 0;
					boolean deleted;
					do {
						deleted = this.isDeleted(dir);
						if (deleted) {
							if (log.isDebugEnabled()) {
								log.debug("Deleted [" + dir + "]");
							}
							break;
						}
						if (log.isDebugEnabled()) {
							log.debug("Failed to delete [" + dir + "]");
						}
						// wait a bit and try again
						count++;
						try {
							Thread.sleep(2);
						}
						catch (InterruptedException e1) {
							Thread.currentThread().interrupt();
							break;
						}

					}
					while (count < maxTries);
					if (!deleted) {
						if (log.isDebugEnabled()) {
							log.debug("Failed to delete [" + dir + "] after [" + maxTries + "] attempts to do it");
						}
						throw new DirectoryNotEmptyException(dir.toString());
					}
					return FileVisitResult.CONTINUE;
				}
				throw exc;
			}

			private boolean isDeleted(Path dir) throws IOException {
				try {
					Files.delete(dir);
					return true;
				}
				catch (DirectoryNotEmptyException e) {
					// happens sometimes if Windows is too slow to remove children of a
					// directory
					return false;
				}
			}
		});
		return true;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path relativePath = this.to.resolve(this.from.relativize(file));
		Files.copy(file, relativePath, StandardCopyOption.REPLACE_EXISTING);
		if (log.isDebugEnabled()) {
			log.debug("Copied file from [" + file.toString() + "] to [" + relativePath.toString() + "]");
		}
		return FileVisitResult.CONTINUE;
	}

}
