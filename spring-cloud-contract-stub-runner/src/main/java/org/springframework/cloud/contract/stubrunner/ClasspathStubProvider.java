package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Stub downloader that picks stubs and contracts from the provided resource.
 * If no {@link org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties#repositoryRoot}
 * is provided then by default classpath is searched according to what has been passed in
 * {@link org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties#ids}. The
 * pattern to search for stubs looks like this
 *
 * <ul>
 * <li>{@code META-INF/group.id/artifactid/ ** /*.* }</li>
 * <li>{@code contracts/group.id/artifactid/ ** /*.* }</li>
 * <li>{@code mappings/group.id/artifactid/ ** /*.* }</li>
 * </ul>
 * <p>
 *
 * examples
 *
 * <p>
 * <ul>
 * <li>{@code META-INF/com.example/fooservice/1.0.0/ **}</li>
 * <li>{@code contracts/com.example/artifactid/ ** /*.* }</li>
 * <li>{@code mappings/com.example/artifactid/ ** /*.* }</li>
 * </ul>
 *
 * @author Marcin Grzejszczak
 * @since 1.1.1
 */
public class ClasspathStubProvider implements StubDownloaderBuilder {

	private static final Log log = LogFactory
			.getLog(MethodHandles.lookup().lookupClass());

	private static final int TEMP_DIR_ATTEMPTS = 10000;
	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
			new DefaultResourceLoader());

	@Override
	public StubDownloader build(final StubRunnerOptions stubRunnerOptions) {
		return new StubDownloader() {
			@Override
			public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
					StubConfiguration config) {
				List<RepoRoot> repoRoots = repoRoot(stubRunnerOptions, config);
				List<String> paths = toPaths(repoRoots);
				List<Resource> resources = resolveResources(paths);
				if (log.isDebugEnabled()) {
					log.debug("For paths " + paths + " found following resources " + resources);
				}
				final File tmp = createTempDir();
				tmp.deleteOnExit();
				Pattern groupAndArtifactPattern = Pattern.compile(
						"^(.*)(" + config.getGroupId() + "." + config.getArtifactId() + ")(.*)$");
				String version = config.getVersion();
				for (Resource resource : resources) {
					try {
						String relativePath = relativePathPicker(resource, groupAndArtifactPattern);
						int lastIndexOf = relativePath.lastIndexOf(File.separator);
						String relativePathWithoutFile = lastIndexOf > -1 ?
								relativePath.substring(0, lastIndexOf) :
								relativePath;
						Path directory = Files.createDirectories(
								new File(tmp, relativePathWithoutFile).toPath());
						File newFile = new File(directory.toFile(), resource.getFilename());
						if (!newFile.exists() && !isDirectory(resource)) {
							Files.copy(resource.getInputStream(), newFile.toPath());
						}
						if (log.isDebugEnabled()) {
							log.debug("Stored file [" + newFile + "]");
						}
					} catch (IOException e) {
						log.error("Exception occurred while trying to create dirs", e);
						throw new IllegalStateException(e);
					}
				}
				log.info("Unpacked files for [" + config.getGroupId() + ":" + config.getArtifactId()
								+ ":" + version + "] to folder [" + tmp + "]");
				return new AbstractMap.SimpleEntry<>(
						new StubConfiguration(config.getGroupId(), config.getArtifactId(), version,
								config.getClassifier()), tmp);
			}

			boolean isDirectory(Resource resource) {
				try {
					return resource.getFile().isDirectory();
				} catch (Exception e) {
					if (log.isTraceEnabled()) {
						log.trace("Exception occurred while trying to convert path to file for resource [" + resource + "]", e);
					}
					return false;
				}
			}

			String relativePathPicker(Resource resource,
					Pattern groupAndArtifactPattern) throws IOException {
				String uri = resource.getURI().toString();
				Matcher groupAndArtifactMatcher = groupAndArtifactPattern.matcher(uri);
				if (groupAndArtifactMatcher.matches()) {
					MatchResult groupAndArtifactResult = groupAndArtifactMatcher
							.toMatchResult();
					return groupAndArtifactResult.group(2) + File.separator
							+ groupAndArtifactResult.group(3);
				}
				else {
					throw new IllegalArgumentException("Illegal uri [${uri}]");
				}
			}
		};
	}

	private List<String> toPaths(List<RepoRoot> repoRoots) {
		List<String> list = new ArrayList<>();
		for (RepoRoot repoRoot : repoRoots) {
			list.add(repoRoot.fullPath);
		}
		return list;
	}

	List<Resource> resolveResources(List<String> paths) {
		List<Resource> resources = new ArrayList<>();
		for (String path : paths) {
			try {
				List<Resource> list = Arrays.asList(this.resolver.getResources(path));
				resources.addAll(list);
			}
			catch (IOException e) {
				log.error("Exception occurred while trying to fetch resources from [" + path + "]");
				throw new IllegalStateException(e);
			}
		}
		return resources;
	}

	private List<RepoRoot> repoRoot(StubRunnerOptions stubRunnerOptions,
			StubConfiguration configuration) {
		if (StringUtils.hasText(stubRunnerOptions.getStubRepositoryRoot())) {
			return Collections
					.singletonList(new RepoRoot(stubRunnerOptions.getStubRepositoryRoot()));
		}
		else {
			String path = "/**/" + configuration.getGroupId() + "/" + configuration.getArtifactId();
			return Arrays.asList(new RepoRoot("classpath*:/META-INF" + path, "/**/*.*"),
					new RepoRoot("classpath*:/contracts" + path, "/**/*.*"),
					new RepoRoot("classpath*:/mappings" + path, "/**/*.*"));
		}
	}

	// Taken from Guava
	private File createTempDir() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException(
				"Failed to create directory within " + TEMP_DIR_ATTEMPTS
						+ " attempts (tried " + baseName + "0 to " + baseName + (
						TEMP_DIR_ATTEMPTS - 1) + ")");
	}

	private static class RepoRoot {
		final String repoRoot;
		final String fullPath;

		RepoRoot(String repoRoot) {
			this.repoRoot = repoRoot;
			this.fullPath = repoRoot + "";
		}

		RepoRoot(String repoRoot, String suffix) {
			this.repoRoot = repoRoot;
			this.fullPath = repoRoot + suffix;
		}
	}
}
