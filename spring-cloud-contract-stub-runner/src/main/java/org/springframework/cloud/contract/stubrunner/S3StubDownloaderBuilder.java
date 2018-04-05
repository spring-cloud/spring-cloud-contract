package org.springframework.cloud.contract.stubrunner;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * Fetch stubs from an S3 bucket
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public final class S3StubDownloaderBuilder implements StubDownloaderBuilder {

	private static final Log log = LogFactory.getLog(S3StubDownloaderBuilder.class);
	private static final String S3_RESOURCE_CLASS_NAME = "org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader";
	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections
			.singletonList("s3");

	/**
	 * Does any of the accepted protocols matches the URL of the repository
	 * @param url - of the repository
	 */
	private static boolean isProtocolAccepted(String url) {
		return ACCEPTABLE_PROTOCOLS.stream().anyMatch(url::startsWith);
	}

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() != StubRunnerProperties.StubsMode.REMOTE ||
				stubRunnerOptions.getStubRepositoryRoot() == null ||
				!isProtocolAccepted(stubRunnerOptions.getOriginalRepositoryRoot()) ||
				s3ResourceClassMissing()) {
			return null;
		}
		return new S3StubDownloader(stubRunnerOptions);
	}

	private static boolean s3ResourceClassMissing() {
		try {
			Class.forName(S3_RESOURCE_CLASS_NAME);
			return false;
		} catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug("[" + S3_RESOURCE_CLASS_NAME + "] is not present on classpath.");
			}
			return true;
		}
	}

	@Override public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (StringUtils.isEmpty(location) || !isProtocolAccepted(location) ||
				s3ResourceClassMissing()) {
			return null;
		}
		String locationWithMetaInf = location;
		if (!location.contains("/META-INF")) {
			locationWithMetaInf = (location.endsWith("/") ? location : location + "/") + "META-INF";
		}
		return s3ResourceLoader().getResource(locationWithMetaInf);
	}

	static ResourceLoader s3ResourceLoader() {
		// TODO: Read props from the props map too
		return new SimpleStorageResourceLoader(AmazonS3ClientBuilder.defaultClient());
	}
}

class S3StubDownloader implements StubDownloader {
	private static final Log log = LogFactory.getLog(S3StubDownloader.class);

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;

	private final StubRunnerOptions stubRunnerOptions;
	private final boolean deleteStubsAfterTest;
	private final FetchedS3Resource s3ResourceFetcher;

	S3StubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.deleteStubsAfterTest = this.stubRunnerOptions.isDeleteStubsAfterTest();
		this.s3ResourceFetcher = new FetchedS3Resource(this.stubRunnerOptions);
		registerShutdownHook();
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> TemporaryFileStorage.cleanup(S3StubDownloader.this.deleteStubsAfterTest)));
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		if (StringUtils.isEmpty(stubConfiguration.version) || "+".equals(stubConfiguration.version)) {
			throw new IllegalStateException("Concrete version wasn't passed for [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]");
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("Trying to find a contract for [" + stubConfiguration.toColonSeparatedDependencyNotation() + "]");
			}
			Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
			File file = this.s3ResourceFetcher.fetchedResource(repo, stubConfiguration);
			if (file == null) {
				return null;
			}
			return new AbstractMap.SimpleEntry<>(stubConfiguration, file);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}

class FetchedS3Resource {

	private static final Log log = LogFactory.getLog(GitContractsRepo.class);

	private static final String TEMP_DIR_PREFIX = "s3-contracts";
	static final Map<Resource, File> CACHED_LOCATIONS = new ConcurrentHashMap<>();

	private final StubRunnerOptions options;

	FetchedS3Resource(StubRunnerOptions options) {
		this.options = options;
	}

	File fetchedResource(Resource repo, StubConfiguration stubConfiguration) throws IOException {
		File file = CACHED_LOCATIONS.get(repo);
		if (file == null) {
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.createTempDir(TEMP_DIR_PREFIX);
			if (log.isDebugEnabled()) {
				log.debug("The files haven't been downloaded. Will download them to [" + tmpDirWhereStubsWillBeUnzipped + "]");
			}
			String antPath = prefixedOriginalUri() + "META-INF/" + stubConfiguration.groupId + "/" + stubConfiguration.artifactId + "/" + stubConfiguration.version + "/**";
			ResourceLoader loader = S3StubDownloaderBuilder.s3ResourceLoader();
			ResourcePatternResolver resolver = new PathMatchingSimpleStorageResourcePatternResolver(AmazonS3ClientBuilder.defaultClient(),
					loader, new PathMatchingResourcePatternResolver());
			log.info("Downloading file matching ant pattern [" + antPath + "]");
			Resource[] filesForVersion = resolver.getResources(antPath);
			if (filesForVersion == null || filesForVersion.length == 0) {
				log.warn("No matching files were found in the S3 bucket");
				return null;
			}
			log.info("Downloaded [" + filesForVersion.length + "] matching files");
			for (Resource resource : filesForVersion) {
				try (InputStream inputStream = resource.getInputStream()) {
					Path path = new File(tmpDirWhereStubsWillBeUnzipped,
							resource.getFilename()).toPath();
					Files.copy(inputStream, path);
					if (log.isDebugEnabled()) {
						log.debug("Successfully stored file to path [" + path + "]");
					}
				}
			}
			CACHED_LOCATIONS.put(repo, tmpDirWhereStubsWillBeUnzipped);
			return tmpDirWhereStubsWillBeUnzipped;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("The project has already been downloaded to [" + file + "]. Will reuse that location");
			}
		}
		return file;
	}

	private String prefixedOriginalUri() {
		return this.options.originalRepositoryRoot.endsWith("/") ?
				this.options.originalRepositoryRoot : (this.options.originalRepositoryRoot + "/");
	}
}