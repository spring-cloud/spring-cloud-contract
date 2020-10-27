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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;

/**
 * Abstraction over a Git repo. Can cloned repo from a given location and check its
 * branch.
 *
 * taken from: https://github.com/spring-cloud/spring-cloud-release-tools
 *
 * @author Marcin Grzejszczak
 */
class GitRepo {

	private static final Log log = LogFactory.getLog(GitRepo.class);

	final JGitFactory gitFactory;

	private final File basedir;

	private final boolean ensureGitSuffix;

	GitRepo(File basedir, GitStubDownloaderProperties properties) {
		this.basedir = basedir;
		this.gitFactory = new JGitFactory(properties);
		this.ensureGitSuffix = properties.ensureGitSuffix;
	}

	// for tests
	@Deprecated
	GitRepo(File basedir) {
		this.basedir = basedir;
		this.gitFactory = new JGitFactory();
		this.ensureGitSuffix = true;
	}

	// for tests
	@Deprecated
	GitRepo(File basedir, boolean ensureGitSuffix) {
		this.basedir = basedir;
		this.gitFactory = new JGitFactory();
		this.ensureGitSuffix = ensureGitSuffix;
	}

	// for tests
	@Deprecated
	GitRepo(File basedir, JGitFactory factory) {
		this.basedir = basedir;
		this.gitFactory = factory;
		this.ensureGitSuffix = true;
	}

	/**
	 * Clones the project.
	 * @param projectUri - URI of the project
	 * @return file where the project was cloned
	 */
	File cloneProject(URI projectUri) {
		try {
			log.info("Cloning repo from [" + projectUri + "] to [" + this.basedir + "]");
			Git git = cloneToBasedir(projectUri, this.basedir);
			if (git != null) {
				git.close();
			}
			File clonedRepo = git.getRepository().getWorkTree();
			log.info("Cloned repo to [" + clonedRepo + "]");
			return clonedRepo;
		}
		catch (Exception e) {
			throw new IllegalStateException("Exception occurred while cloning repo", e);
		}
	}

	/**
	 * Checks out a branch for a project.
	 * @param project - a Git project
	 * @param branch - branch to check out
	 */
	void checkout(File project, String branch) {
		try {
			String currentBranch = currentBranch(project);
			if (currentBranch.equals(branch)) {
				log.info("Won't check out the same branch. Skipping");
				return;
			}
			log.info("Checking out branch [" + branch + "]");
			checkoutBranch(project, branch);
			log.info("Successfully checked out the branch [" + branch + "]");
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Pulls changes for the project.
	 * @param project - a Git project
	 */
	void pull(File project) {
		try {
			try (Git git = this.gitFactory.open(project)) {
				PullCommand command = this.gitFactory.pull(git);
				command.setRebase(true).call();
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Performs a commit.
	 * @param project - a Git project
	 * @param message - commit message
	 * @return result of the commit
	 */
	CommitResult commit(File project, String message) {
		try (Git git = this.gitFactory.open(file(project))) {
			git.add().addFilepattern(".").call();
			git.commit().setAllowEmpty(false).setMessage(message).call();
			log.info("Commited successfully with message [" + message + "]");
			return CommitResult.SUCCESSFUL;
		}
		catch (EmptyCommitException e) {
			log.info("There were no changes detected. Will not commit an empty commit");
			return CommitResult.EMPTY;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	void reset(File project) {
		try (Git git = this.gitFactory.open(file(project))) {
			git.reset().setMode(ResetCommand.ResetType.HARD).call();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Pushes the commits od current branch.
	 * @param project - Git project
	 */
	void pushCurrentBranch(File project) {
		try (Git git = this.gitFactory.open(file(project))) {
			this.gitFactory.push(git).call();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private File file(File project) throws FileNotFoundException {
		return ResourceUtils.getFile(project.toURI()).getAbsoluteFile();
	}

	private Git cloneToBasedir(URI projectUrl, File destinationFolder) {
		String projectGitUrl = sanitizeGitUrl(projectUrl);
		if (log.isDebugEnabled()) {
			log.debug("Project git url [" + projectGitUrl + "]");
		}
		CloneCommand command = this.gitFactory.getCloneCommandByCloneRepository()
				.setURI(projectGitUrl).setDirectory(destinationFolder);
		try {
			Git git = command.call();
			if (git.getRepository().getRemoteNames().isEmpty()) {
				log.info("No remote added. Will add remote of the cloned project");
				git.remoteSetUrl().setUri(new URIish(projectGitUrl));
				git.remoteSetUrl().setName("origin");
				git.remoteSetUrl().setPush(true);
			}
			return git;
		}
		catch (GitAPIException | URISyntaxException e) {
			deleteBaseDirIfExists("Failed to initialize base directory");
			throw new IllegalStateException(e);
		}
	}

	protected String sanitizeGitUrl(URI uri) {
		String urlString = uri.toString();
		return (urlString.endsWith(".git") || !this.ensureGitSuffix) ? urlString
				: urlString + ".git";
	}

	private Ref checkoutBranch(File projectDir, String branch) throws GitAPIException {
		Git git = this.gitFactory.open(projectDir);
		CheckoutCommand command = git.checkout().setName(branch);
		try {
			if (shouldTrack(git, branch)) {
				trackBranch(command, branch);
			}
			return command.call();
		}
		catch (GitAPIException e) {
			deleteBaseDirIfExists("Failed to delete base directory");
			throw e;
		}
		finally {
			git.close();
		}
	}

	private String currentBranch(File projectDir) {
		Git git = this.gitFactory.open(projectDir);
		try {
			return git.getRepository().getBranch();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			git.close();
		}
	}

	private boolean shouldTrack(Git git, String label) throws GitAPIException {
		return isBranch(git, label) && !isLocalBranch(git, label);
	}

	private void trackBranch(CheckoutCommand checkout, String label) {
		checkout.setCreateBranch(true).setName(label)
				.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
				.setStartPoint("origin/" + label);
	}

	private boolean isBranch(Git git, String label) throws GitAPIException {
		return containsBranch(git, label, ListBranchCommand.ListMode.ALL);
	}

	private boolean isLocalBranch(Git git, String label) throws GitAPIException {
		return containsBranch(git, label, null);
	}

	private boolean containsBranch(Git git, String label,
			ListBranchCommand.ListMode listMode) throws GitAPIException {
		ListBranchCommand command = git.branchList();
		if (listMode != null) {
			command.setListMode(listMode);
		}
		List<Ref> branches = command.call();
		for (Ref ref : branches) {
			if (ref.getName().endsWith("/" + label)) {
				return true;
			}
		}
		return false;
	}

	private void deleteBaseDirIfExists(String errorMessage) {
		if (this.basedir.exists()) {
			if (!FileSystemUtils.deleteRecursively(this.basedir)) {
				throw new IllegalStateException(errorMessage);
			}
		}
	}

	enum CommitResult {

		SUCCESSFUL, EMPTY

	}

	/**
	 * Wraps the static method calls to {@link Git} and {@link CloneCommand} allowing for
	 * easier unit testing.
	 */
	static class JGitFactory {

		private static final Logger log = LoggerFactory
				.getLogger(MethodHandles.lookup().lookupClass());

		final CredentialsProvider provider;

		private final JschConfigSessionFactory factory = new JschConfigSessionFactory() {

			@Override
			protected void configure(OpenSshConfig.Host host, Session session) {
			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				Connector connector = null;
				try {
					if (SSHAgentConnector.isConnectorAvailable()) {
						USocketFactory usf = new JNAUSocketFactory();
						connector = new SSHAgentConnector(usf);
					}
					log.info("Successfully connected to an agent");
				}
				catch (AgentProxyException e) {
					log.error(
							"Exception occurred while trying to connect to agent. Will create"
									+ "the default JSch connection",
							e);
					return super.createDefaultJSch(fs);
				}
				final JSch jsch = super.createDefaultJSch(fs);
				if (connector != null) {
					JSch.setConfig("PreferredAuthentications", "publickey,password");
					IdentityRepository identityRepository = new RemoteIdentityRepository(
							connector);
					jsch.setIdentityRepository(identityRepository);
				}
				return jsch;
			}
		};

		private final TransportConfigCallback callback = transport -> {
			if (transport instanceof SshTransport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(this.factory);
			}
		};

		JGitFactory(GitStubDownloaderProperties properties) {
			if (org.springframework.util.StringUtils.hasText(properties.username)) {
				log.info(
						"Passed username and password - will set a custom credentials provider");
				this.provider = credentialsProvider(properties);
			}
			else {
				if (log.isDebugEnabled()) {
					log.debug("No custom credentials provider will be set");
				}
				this.provider = null;
			}
		}

		// for tests
		JGitFactory() {
			this.provider = null;
		}

		CredentialsProvider credentialsProvider(GitStubDownloaderProperties properties) {
			return new UsernamePasswordCredentialsProvider(properties.username,
					properties.password);
		}

		CloneCommand getCloneCommandByCloneRepository() {
			return Git.cloneRepository().setCredentialsProvider(this.provider)
					.setTransportConfigCallback(this.callback);
		}

		PushCommand push(Git git) {
			return git.push().setCredentialsProvider(this.provider)
					.setTransportConfigCallback(this.callback);
		}

		PullCommand pull(Git git) {
			return git.pull().setCredentialsProvider(this.provider)
					.setTransportConfigCallback(this.callback);
		}

		Git open(File file) {
			try {
				return Git.open(file);
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

	}

}
