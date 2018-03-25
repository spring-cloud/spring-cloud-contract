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
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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

/**
 * Builds a {@link StubDownloader} to work with contracts and stubs in a git repo
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class GitStubDownloaderBuilder implements StubDownloaderBuilder {

	private static final String GIT_PROTOCOL = "git";

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH ||
				stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		if (!(resource instanceof GitResource)) {
			return null;
		}
		return new GitStubDownloader(stubRunnerOptions);
	}

	@Override public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!StringUtils.hasText(location) || !location.startsWith(GIT_PROTOCOL)) {
			return null;
		}
		return new GitResource(location);
	}
}


/**
 * Primitive version of a Git {@link Resource}
 */
class GitResource extends AbstractResource {

	private final String rawLocation;

	GitResource(String location) {
		this.rawLocation = location;
	}

	@Override public String getDescription() {
		return this.rawLocation;
	}

	@Override public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override public URI getURI() throws IOException {
		return URI.create(this.rawLocation);
	}
}


class GitStubDownloader implements StubDownloader {

	private static final String TEMP_DIR_PREFIX = "git-contracts";

	private final StubRunnerOptions stubRunnerOptions;
	private final boolean deleteStubsAfterTest;
	private static final Map<Resource, File> CACHED_LOCATIONS = new ConcurrentHashMap<>();

	GitStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.deleteStubsAfterTest = this.stubRunnerOptions.isDeleteStubsAfterTest();
		registerShutdownHook();
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		if (!StringUtils.hasText(stubConfiguration.version) || "+".equals(stubConfiguration.version)) {
			throw new IllegalStateException("Concrete version wasn't passed for [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]");
		}
		try {
			Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
			//TODO: Checking out branches
			File clonedRepo = clonedRepo(repo);
				FileWalker walker = new FileWalker(stubConfiguration);
			Files.walkFileTree(clonedRepo.toPath(), walker);
			if (walker.foundFile != null) {
				return new AbstractMap.SimpleEntry<>(stubConfiguration, walker.foundFile.toFile());
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return null;
	}

	private File clonedRepo(Resource repo) throws IOException {
		File file = CACHED_LOCATIONS.get(repo);
		if (file == null) {
			GitStubDownloaderProperties properties = new GitStubDownloaderProperties(repo, this.stubRunnerOptions.getProperties());
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.unpackStubJarToATemporaryFolder(TEMP_DIR_PREFIX);
			GitRepo gitRepo = new GitRepo(tmpDirWhereStubsWillBeUnzipped, properties);
			file = gitRepo.cloneProject(properties.url);
			gitRepo.checkout(file, properties.branch);
			CACHED_LOCATIONS.put(repo, file);
		}
		return file;
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(
				() -> TemporaryFileStorage.cleanup(GitStubDownloader.this.deleteStubsAfterTest)));
	}
}

class GitStubDownloaderProperties {
	private static final String GIT_BRANCH_PROPERTY = "git.branch";
	private static final String GIT_USERNAME_PROPERTY = "git.username";
	private static final String GIT_PASSWORD_PROPERTY = "git.password";

	final URI url;
	final String username;
	final String password;
	final String branch;

	GitStubDownloaderProperties(Resource repo, Map<String, String> args)
			throws IOException {
		String repoUrl = schemeSpecificPart(repo.getURI());
		// if we had git://https://... we want the part starting from https
		// if we had git://git@... we want the full address again
		this.url = URI.create(repoUrl.startsWith("git@") ? "git://" + repoUrl : repoUrl);
		this.username = StubRunnerPropertyUtils.getProperty(args, GIT_USERNAME_PROPERTY);
		this.password = StubRunnerPropertyUtils.getProperty(args, GIT_PASSWORD_PROPERTY);
		String branch = StubRunnerPropertyUtils.getProperty(args, GIT_BRANCH_PROPERTY);
		this.branch = StringUtils.hasText(branch) ? branch : "master";
	}

	private String schemeSpecificPart(URI uri) {
		String part = uri.getSchemeSpecificPart();
		return StringUtils.hasText(part) && part.startsWith("//") ? part.substring(2) : part;
	}
}

/**
 * Abstraction over a Git repo. Can cloned repo from a given location
 * and check its branch.
 *
 * taken from: https://github.com/spring-cloud/spring-cloud-release-tools
 *
 * @author Marcin Grzejszczak
 */
class GitRepo {

	private static final Logger log = LoggerFactory.getLogger(GitRepo.class);

	private final GitRepo.JGitFactory gitFactory;

	private final File basedir;

	GitRepo(File basedir, GitStubDownloaderProperties properties) {
		this.basedir = basedir;
		this.gitFactory = new GitRepo.JGitFactory(properties);
	}

	// for tests
	GitRepo(File basedir) {
		this.basedir = basedir;
		this.gitFactory = new GitRepo.JGitFactory();
	}

	// for tests
	GitRepo(File basedir, GitRepo.JGitFactory factory) {
		this.basedir = basedir;
		this.gitFactory = factory;
	}

	/**
	 * Clones the project
	 * @param projectUri - URI of the project
	 * @return file where the project was cloned
	 */
	File cloneProject(URI projectUri) {
		try {
			log.info("Cloning repo from [{}] to [{}]", projectUri, this.basedir);
			Git git = cloneToBasedir(projectUri, this.basedir);
			if (git != null) {
				git.close();
			}
			File clonedRepo = git.getRepository().getWorkTree();
			log.info("Cloned repo to [{}]", clonedRepo);
			return clonedRepo;
		}
		catch (Exception e) {
			throw new IllegalStateException("Exception occurred while cloning repo", e);
		}
	}

	/**
	 * Checks out a branch for a project
	 * @param project - a Git project
	 * @param branch - branch to check out
	 */
	void checkout(File project, String branch) {
		try {
			log.info("Checking out branch [{}] for repo [{}]", branch, this.basedir);
			checkoutBranch(project, branch);
			log.info("Successfully checked out the branch [{}]", branch);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Git cloneToBasedir(URI projectUrl, File destinationFolder)
			throws GitAPIException {
		String projectString = projectUrl.toString();
		projectString = projectString.startsWith("git://") ? projectString.substring("git://".length()) : projectString;
		projectString = projectString.endsWith(".git") ? projectString.substring(0, projectString.indexOf(".git")) : projectString;
		CloneCommand command = this.gitFactory.getCloneCommandByCloneRepository()
				.setURI(projectString + ".git").setDirectory(destinationFolder);
		try {
			return command.call();
		}
		catch (GitAPIException e) {
			deleteBaseDirIfExists();
			throw e;
		}
	}

	private Ref checkoutBranch(File projectDir, String branch)
			throws GitAPIException {
		Git git = this.gitFactory.open(projectDir);
		CheckoutCommand command = git.checkout().setName(branch);
		try {
			if (shouldTrack(git, branch)) {
				trackBranch(command, branch);
			}
			return command.call();
		}
		catch (GitAPIException e) {
			deleteBaseDirIfExists();
			throw e;
		} finally {
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

	private boolean containsBranch(Git git, String label, ListBranchCommand.ListMode listMode)
			throws GitAPIException {
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

	private void deleteBaseDirIfExists() {
		if (this.basedir.exists()) {
			try {
				FileUtils.delete(this.basedir, FileUtils.RECURSIVE);
			}
			catch (IOException e) {
				throw new IllegalStateException("Failed to initialize base directory", e);
			}
		}
	}

	/**
	 * Wraps the static method calls to {@link org.eclipse.jgit.api.Git} and
	 * {@link org.eclipse.jgit.api.CloneCommand} allowing for easier unit testing.
	 */
	static class JGitFactory {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private final JschConfigSessionFactory factory = new JschConfigSessionFactory() {

			@Override protected void configure(OpenSshConfig.Host host, Session session) {
			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				Connector connector = null;
				try {
					if(SSHAgentConnector.isConnectorAvailable()){
						USocketFactory usf = new JNAUSocketFactory();
						connector = new SSHAgentConnector(usf);
					}
					log.info("Successfully connected to an agent");
				} catch (AgentProxyException e) {
					log.error("Exception occurred while trying to connect to agent. Will create"
							+ "the default JSch connection", e);
					return super.createDefaultJSch(fs);
				}
				final JSch jsch = super.createDefaultJSch(fs);
				if (connector != null) {
					JSch.setConfig("PreferredAuthentications", "publickey,password");
					IdentityRepository identityRepository = new RemoteIdentityRepository(connector);
					jsch.setIdentityRepository(identityRepository);
				}
				return jsch;
			}
		};

		private final CredentialsProvider provider;

		JGitFactory(GitStubDownloaderProperties properties) {
			if (org.springframework.util.StringUtils.hasText(properties.username)) {
				log.info("Passed username and password - will set a custom credentials provider");
				this.provider = credentialsProvider(properties);
			} else {
				log.info("No custom credentials provider will be set");
				this.provider = null;
			}
		}

		CredentialsProvider credentialsProvider(GitStubDownloaderProperties properties) {
			return new UsernamePasswordCredentialsProvider(
					properties.username,
					properties.password);
		}

		// for tests
		JGitFactory() {
			this.provider = null;
		}

		private final TransportConfigCallback callback = transport -> {
			if (transport instanceof SshTransport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(this.factory);
			}
		};

		CloneCommand getCloneCommandByCloneRepository() {
			return Git.cloneRepository()
					.setCredentialsProvider(this.provider)
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

class FileWalker extends SimpleFileVisitor<Path> {

	private final PathMatcher matcherWithDot;
	private final PathMatcher matcherWithoutDot;
	Path foundFile;

	FileWalker(StubConfiguration stubConfiguration) {
		this.matcherWithDot = FileSystems.getDefault()
				.getPathMatcher("glob:" + matcherGlob(stubConfiguration, "."));
		this.matcherWithoutDot = FileSystems.getDefault()
				.getPathMatcher("glob:" + matcherGlob(stubConfiguration, "/"));
	}

	private String matcherGlob(StubConfiguration stubConfiguration, String groupArtifactSeparator) {
		return "**" + stubConfiguration.groupId + groupArtifactSeparator
				+ stubConfiguration.artifactId + "/"
				+ stubConfiguration.version;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		if (this.matcherWithDot.matches(dir.toAbsolutePath()) ||
				this.matcherWithoutDot.matches(dir.toAbsolutePath())) {
			this.foundFile = dir;
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}
}