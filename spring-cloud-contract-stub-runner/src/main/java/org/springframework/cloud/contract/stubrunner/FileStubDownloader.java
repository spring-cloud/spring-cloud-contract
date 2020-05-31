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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Allows to read stubs and contracts from a given location. Contrary to
 * {@link org.springframework.cloud.contract.stubrunner.AetherStubDownloaderBuilder},
 * doesn't require the location to be a maven repository.
 *
 * @author Marcin Grzejszczak
 */
public class FileStubDownloader implements StubDownloaderBuilder {

	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections
			.singletonList("stubs");

	/**
	 * Does any of the accepted protocols matches the URL of the repository.
	 * @param url - of the repository
	 * @return {@code true} if protocol is accepted
	 */
	public static boolean isProtocolAccepted(String url) {
		return ACCEPTABLE_PROTOCOLS.stream().anyMatch(url::startsWith);
	}

	@Override
	public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		// should work only in remote and local option
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH
				|| stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		// we verify whether the protocol starts with `stubs://`
		if (!(resource instanceof StubsResource)) {
			return null;
		}
		return new StubsStubDownloader(stubRunnerOptions);
	}

	@Override
	public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (StringUtils.isEmpty(location) || !isProtocolAccepted(location)) {
			return null;
		}
		// Can be resolving a resource for Classpath as fallback
		if (!location.startsWith("stubs://file://")) {
			return new StubsResource(location);
		}
		// Convert any windows file format path to a uri
		String correctlyFormattedLocation = convertLocationToUriFormat(location);
		return new StubsResource(correctlyFormattedLocation);
	}

	private String convertLocationToUriFormat(String location) {
		final String correctlyFormattedLocation = FilenameUtils
				.separatorsToUnix(location);
		final String rawPath = correctlyFormattedLocation.replace("stubs://file://", "");
		if (rawPath.charAt(0) != '/') {
			return "stubs://file:///" + rawPath;
		}
		return correctlyFormattedLocation;
	}

}

/**
 * Primitive version of a Stubs {@link Resource}. Automatically makes Spring convert the
 * URL to a Resource.
 */
class StubsResource extends AbstractResource {

	private final String rawLocation;

	StubsResource(String location) {
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

/**
 * Concrete logic of picking stubs.
 */
class StubsStubDownloader implements StubDownloader {

	private static final Log log = LogFactory.getLog(StubsStubDownloader.class);

	private static final String STUBS_FIND_PRODUCER_PROPERTY = "stubs.find-producer";

	private static final String LATEST_VERSION = "+";

	private final StubRunnerOptions stubRunnerOptions;

	StubsStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
	}

	// StubConfiguration is the concrete stub to be fetched
	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		boolean shouldFindProducer = shouldFindProducer();
		if (!shouldFindProducer) {
			String schemeSpecific = schemeSpecificPart();
			log.info("Stubs are present under [" + schemeSpecific
					+ "]. Will copy them to a temporary directory.");
			return new ResourceResolvingStubDownloader(stubRunnerOptions,
					this::repoRootForSchemeSpecificPart, this::anyPattern)
					.downloadAndUnpackStubJar(stubConfiguration);
		}
		return new ResourceResolvingStubDownloader(stubRunnerOptions, this::repoRoot,
				this::gavPattern).downloadAndUnpackStubJar(stubConfiguration);
	}

	private RepoRoots repoRootForSchemeSpecificPart(StubRunnerOptions stubRunnerOptions, StubConfiguration configuration) {
		String specificPart = schemeSpecificPart();
		specificPart = specificPart.endsWith("/") ? specificPart : (specificPart + "/");
		specificPart = specificPart + "**/*.*";
		return new RepoRoots(Collections.singleton(new RepoRoot(specificPart)));
	}

	private Pattern anyPattern(StubConfiguration config) {
		return Pattern.compile(resolvePath() + "(.*)");
	}

	private String resolvePath() {
		String schemeSpecificPart = schemeSpecificPart();
		Resource resource = ResourceResolver.resource(schemeSpecificPart);
		if (resource != null) {
			try {
				return resource.getURL().getFile();
			}
			catch (IOException ex) {
				return schemeSpecificPart;
			}
		}
		return schemeSpecificPart;
	}

	// for group id a.b.c and artifact id d
	// a.b.c/d
	// a/b/c/d
	private RepoRoots repoRoot(StubRunnerOptions stubRunnerOptions, StubConfiguration configuration) {
		String pathWithGroupAndArtifactId = "/" + configuration.getGroupId() + "/"
				+ configuration.getArtifactId();
		String pathWithGroupAndArtifactIdSlashSeparated = "/"
				+ configuration.getGroupId().replace(".", File.separator) + "/"
				+ configuration.getArtifactId();
		String anyFileSuffix = "/**/*.*";
		RepoRoots roots = RepoRoots.asList(
				new RepoRoot(schemeSpecificPart() + pathWithGroupAndArtifactId,
						anyFileSuffix),
				new RepoRoot(
						schemeSpecificPart() + pathWithGroupAndArtifactIdSlashSeparated,
						anyFileSuffix),
				new RepoRoot(schemeSpecificPart() + anyFileSuffix));
		if (!latestVersionIsSet(configuration)) {
			String pathWithGAV = pathWithGroupAndArtifactId + "/"
					+ configuration.getVersion();
			String pathWithSlashSeparatedGAV = pathWithGroupAndArtifactIdSlashSeparated
					+ "/" + configuration.getVersion();
			roots.addAll(RepoRoots.asList(
					new RepoRoot(schemeSpecificPart() + pathWithGAV, anyFileSuffix),
					new RepoRoot(schemeSpecificPart() + pathWithSlashSeparatedGAV,
							anyFileSuffix)));
		}
		return roots;
	}

	private Pattern gavPattern(StubConfiguration config) {
		String version = config.getVersion();
		String ga = config.getGroupId() + "." + config.getArtifactId();
		String gav = latestVersionIsSet(config) ? ga : (ga + "." + version);
		return Pattern.compile("^(.*)(" + gav + ")(.*)$");
	}

	private boolean latestVersionIsSet(StubConfiguration configuration) {
		return LATEST_VERSION.equals(configuration.getVersion());
	}

	private boolean shouldFindProducer() {
		Map<String, String> args = this.stubRunnerOptions.getProperties();
		String findProducer = StubRunnerPropertyUtils.getProperty(args,
				STUBS_FIND_PRODUCER_PROPERTY);
		return Boolean.parseBoolean(findProducer);
	}

	// stubs://foo -> foo
	private String schemeSpecificPart() {
		try {
			String part = this.stubRunnerOptions.getStubRepositoryRoot().getURI()
					.getSchemeSpecificPart();
			if (StringUtils.isEmpty(part)) {
				return part;
			}
			return part.startsWith("//") ? part.substring(2) : part;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
