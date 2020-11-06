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
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import shaded.org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Builds a {@link StubDownloader} to work with contracts and stubs from a SCM.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public final class ScmStubDownloaderBuilder implements StubDownloaderBuilder {

	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections.singletonList("git");

	/**
	 * Does any of the accepted protocols matches the URL of the repository.
	 * @param url - of the repository
	 * @return {@code true} if the protocol is accepted
	 */
	public static boolean isProtocolAccepted(String url) {
		return ACCEPTABLE_PROTOCOLS.stream().anyMatch(url::startsWith);
	}

	@Override
	public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH
				|| stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		if (!(resource instanceof GitResource)) {
			return null;
		}
		return new GitStubDownloader(stubRunnerOptions);
	}

	@Override
	public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (StringUtils.isEmpty(location) || !isProtocolAccepted(location)) {
			return null;
		}
		return new GitResource(location);
	}

}

/**
 * Primitive version of a Git {@link Resource}.
 */
class GitResource extends AbstractResource {

	private final String rawLocation;

	GitResource(String location) {
		this.rawLocation = location;
	}

	@Override
	public String getDescription() {
		return this.rawLocation;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public URI getURI() throws IOException {
		return URI.create(this.rawLocation);
	}

}

class GitContractsRepo {

	static final Map<Resource, File> CACHED_LOCATIONS = new ConcurrentHashMap<>();

	private static final Log log = LogFactory.getLog(GitContractsRepo.class);

	private static final String TEMP_DIR_PREFIX = "git-contracts";

	private final StubRunnerOptions options;

	GitContractsRepo(StubRunnerOptions options) {
		this.options = options;
	}

	File clonedRepo(Resource repo) {
		File file = CACHED_LOCATIONS.get(repo);
		GitStubDownloaderProperties properties = new GitStubDownloaderProperties(repo, this.options);
		if (file == null) {
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.createTempDir(TEMP_DIR_PREFIX);
			GitRepo gitRepo = new GitRepo(tmpDirWhereStubsWillBeUnzipped, properties);
			file = gitRepo.cloneProject(properties.url);
			gitRepo.checkout(file, properties.branch);
			CACHED_LOCATIONS.put(repo, file);
			if (log.isDebugEnabled()) {
				log.debug("The project hasn't already been cloned. Cloned it to [" + file + "]");
			}
		}
		else {
			if (log.isDebugEnabled()) {
				log.debug("The project has already been cloned to [" + file
						+ "]. Will reset any changes and pull the latest ones.");
			}
			GitRepo gitRepo = new GitRepo(file, properties);
			gitRepo.reset(file);
			gitRepo.pull(file);
		}
		return file;
	}

}

class GitStubDownloader implements StubDownloader {

	private static final Log log = LogFactory.getLog(GitStubDownloader.class);

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;

	private final StubRunnerOptions stubRunnerOptions;

	private final boolean deleteStubsAfterTest;

	private final GitContractsRepo gitContractsRepo;

	GitStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.deleteStubsAfterTest = this.stubRunnerOptions.isDeleteStubsAfterTest();
		this.gitContractsRepo = new GitContractsRepo(stubRunnerOptions);
		registerShutdownHook();
	}

	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(StubConfiguration stubConfiguration) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Trying to find a contract for [" + stubConfiguration.toColonSeparatedDependencyNotation()
						+ "]");
			}
			Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
			File clonedRepo = this.gitContractsRepo.clonedRepo(repo);
			FileWalker walker = new FileWalker(stubConfiguration);
			Files.walkFileTree(clonedRepo.toPath(), walker);
			if (walker.foundFile != null) {
				return new AbstractMap.SimpleEntry<>(stubConfiguration, walker.foundFile.toFile());
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		if (log.isWarnEnabled()) {
			log.warn("No matching contracts were found in the repo for ["
					+ stubConfiguration.toColonSeparatedDependencyNotation() + "]");
		}
		return null;
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(
				new Thread(() -> TemporaryFileStorage.cleanup(GitStubDownloader.this.deleteStubsAfterTest)));
	}

}

class GitStubDownloaderProperties {

	private static final Log log = LogFactory.getLog(GitStubDownloaderProperties.class);

	private static final String GIT_BRANCH_PROPERTY = "git.branch";

	private static final String GIT_USERNAME_PROPERTY = "git.username";

	private static final String GIT_PASSWORD_PROPERTY = "git.password";

	private static final String GIT_ENSURE_GIT_SUFFIX_PROPERTY = "git.ensure-git-suffix";

	final URI url;

	final String username;

	final String password;

	final String branch;

	final Boolean ensureGitSuffix;

	GitStubDownloaderProperties(Resource repo, StubRunnerOptions options) {
		String repoUrl;
		Map<String, String> args = options.getProperties();
		try {
			repoUrl = schemeSpecificPart(repo.getURI());
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		// if we had git://https://... we want the part starting from https
		// if we had git://git@... we want the full address again
		// if the URL starts with git@... and ends with .git, we want to remove it
		String modifiedRepo = repoUrl.startsWith("git@") ? modifyUrlForGitRepo(repoUrl) : repoUrl;
		this.url = URI.create(modifiedRepo);
		String username = StubRunnerPropertyUtils.getProperty(args, GIT_USERNAME_PROPERTY);
		this.username = StringUtils.hasText(username) ? username : options.getUsername();
		String password = StubRunnerPropertyUtils.getProperty(args, GIT_PASSWORD_PROPERTY);
		this.password = StringUtils.hasText(password) ? password : options.getPassword();
		String branch = StubRunnerPropertyUtils.getProperty(args, GIT_BRANCH_PROPERTY);
		this.branch = StringUtils.hasText(branch) ? branch : "master";
		String ensureGitSuffix = StubRunnerPropertyUtils.getProperty(args, GIT_ENSURE_GIT_SUFFIX_PROPERTY);
		this.ensureGitSuffix = StringUtils.hasText(ensureGitSuffix) ? Boolean.parseBoolean(ensureGitSuffix) : true;

		if (log.isDebugEnabled()) {
			log.debug("Repo url is [" + repoUrl + "], modified url string " + "is [" + modifiedRepo + "] URL is ["
					+ this.url + "]  branch is [" + this.branch + "] and ensureGitSuffix is [" + this.ensureGitSuffix
					+ "]");
		}
	}

	private String schemeSpecificPart(URI uri) {
		String part = uri.getSchemeSpecificPart();
		if (StringUtils.isEmpty(part)) {
			return part;
		}
		return part.startsWith("//") ? part.substring(2) : part;
	}

	private String modifyUrlForGitRepo(String gitRepo) {
		return "git:" + gitRepo;
	}

}

class FileWalker extends SimpleFileVisitor<Path> {

	private static final Log log = LogFactory.getLog(FileWalker.class);

	private static final List<String> LATEST = Arrays.asList("latest", "+");

	private static final String RELEASE = "release";

	private final PathMatcher matcherWithDot;

	private final PathMatcher matcherWithoutDot;

	private final boolean latestSnapshotVersion;

	private final boolean latestReleaseVersion;

	Path foundFile;

	FileWalker(StubConfiguration stubConfiguration) {
		this.latestSnapshotVersion = LATEST.stream().anyMatch(s -> s.equals(stubConfiguration.version.toLowerCase()));
		this.latestReleaseVersion = RELEASE.equals(stubConfiguration.version.toLowerCase());
		this.matcherWithDot = FileSystems.getDefault().getPathMatcher("glob:" + matcherGlob(stubConfiguration, "."));
		this.matcherWithoutDot = FileSystems.getDefault().getPathMatcher("glob:" + matcherGlob(stubConfiguration, "/"));
	}

	private String matcherGlob(StubConfiguration stubConfiguration, String groupArtifactSeparator) {
		return "**" + stubConfiguration.groupId + groupArtifactSeparator + stubConfiguration.artifactId + "/"
				+ (this.latestSnapshotVersion || this.latestReleaseVersion ? "**" : stubConfiguration.version);
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (this.matcherWithDot.matches(dir.toAbsolutePath()) || this.matcherWithoutDot.matches(dir.toAbsolutePath())) {
			if (this.latestSnapshotVersion || this.latestReleaseVersion) {
				// folders with name latest, release
				File[] files = Objects.requireNonNull(dir.getParent().toFile().listFiles(File::isDirectory));
				File file = folderWithPredefinedName(files);
				if (file != null) {
					if (log.isDebugEnabled()) {
						log.debug("Found folder with name corresponding to a latest version [" + file + "] ");
						this.foundFile = file.toPath();
						return FileVisitResult.TERMINATE;
					}
				}
				return latestVersionFromFolders(dir, files);
			}
			else {
				this.foundFile = dir;
			}
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}

	private FileVisitResult latestVersionFromFolders(Path dir, File[] files) {
		List<DefaultArtifactVersionWrapper> versions = pickLatestVersion(files);
		if (versions.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Not a single version matching semver for path [" + dir.toAbsolutePath().toString()
						+ "] was found");
			}
			return FileVisitResult.CONTINUE;
		}
		// 2.0.0.RELEASE, 2.0.0.BUILD-SNAPSHOT
		// 2.0.0.RELEASE
		DefaultArtifactVersionWrapper latestFoundVersion = versions.get(versions.size() - 1);
		latestFoundVersion = replaceWithSnapshotIfSameVersions(versions, latestFoundVersion);
		this.foundFile = latestFoundVersion.file.toPath();
		return FileVisitResult.TERMINATE;
	}

	private DefaultArtifactVersionWrapper replaceWithSnapshotIfSameVersions(
			List<DefaultArtifactVersionWrapper> versions, final DefaultArtifactVersionWrapper latestFoundVersion) {
		if (versions.size() > 1 && this.latestSnapshotVersion) {
			// 2.0.1.BUILD-SNAPSHOT, 2.0.0.BUILD-SNAPSHOT
			// 2.0.0.BUILD-SNAPSHOT, 2.0.0.RELEASE
			DefaultArtifactVersionWrapper sameVersionButSnapshot = versions.stream().filter(
					w -> w.projectVersion.isSameWithoutSuffix(latestFoundVersion.projectVersion) && w.isSnapshot())
					.findFirst().orElse(latestFoundVersion);
			// 2.0.0 vs 2.0.0
			// replace the RELEASE one with SNAPSHOT
			if (sameVersionButSnapshot != latestFoundVersion) {
				return sameVersionButSnapshot;
			}
		}
		return latestFoundVersion;
	}

	private File folderWithPredefinedName(File[] files) {
		if (this.latestSnapshotVersion) {
			return Arrays.stream(files)
					.filter(file -> LATEST.stream().anyMatch(s -> s.equals(file.getName().toLowerCase()))).findFirst()
					.orElse(null);
		}
		return Arrays.stream(files).filter(file -> RELEASE.equals(file.getName().toLowerCase())).findFirst()
				.orElse(null);
	}

	private List<DefaultArtifactVersionWrapper> pickLatestVersion(File[] files) {
		return Arrays.stream(files).map(DefaultArtifactVersionWrapper::new)
				.filter(wrapper -> this.latestSnapshotVersion || wrapper.isNotSnapshot()).sorted()
				.collect(Collectors.toList());
	}

}

class DefaultArtifactVersionWrapper implements Comparable<DefaultArtifactVersionWrapper> {

	final DefaultArtifactVersion version;

	final File file;

	final ProjectVersion projectVersion;

	DefaultArtifactVersionWrapper(File file) {
		this.version = new DefaultArtifactVersion(file.getName());
		this.file = file;
		this.projectVersion = new ProjectVersion(this.version.toString());
	}

	boolean isSnapshot() {
		return this.projectVersion.isSnapshot();
	}

	boolean isNotSnapshot() {
		return !isSnapshot();
	}

	@Override
	public int compareTo(DefaultArtifactVersionWrapper o) {
		return this.projectVersion.isMoreMature(o.projectVersion);
	}

}
