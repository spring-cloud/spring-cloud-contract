/*
 *  Copyright 2013-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactSpecVersion;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.loader.PactBrokerLoader;
import au.com.dius.pact.provider.junit.sysprops.SystemPropertyResolver;
import au.com.dius.pact.provider.junit.sysprops.ValueResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Allows downloading of Pact files from the Pact Broker
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class PactStubDownloaderBuilder implements StubDownloaderBuilder {
	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections
			.singletonList("pact");

	/**
	 * Does any of the accepted protocols matches the URL of the repository
	 * @param url - of the repository
	 */
	public static boolean isProtocolAccepted(String url) {
		return ACCEPTABLE_PROTOCOLS.stream().anyMatch(url::startsWith);
	}

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH ||
				stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		if (!(resource instanceof PactResource)) {
			return null;
		}
		return new PactStubDownloader(stubRunnerOptions);
	}

	@Override public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (StringUtils.isEmpty(location) || !isProtocolAccepted(location)) {
			return null;
		}
		return new PactResource(location);
	}
}

class PactResource extends AbstractResource {

	private final String rawLocation;

	PactResource(String location) {
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

class PactStubDownloader implements StubDownloader {
	private static final String TEMP_DIR_PREFIX = "pact";
	private static final Log log = LogFactory.getLog(PactStubDownloader.class);

	private final StubRunnerOptions stubRunnerOptions;
	private final boolean deleteStubsAfterTest;
	private final ObjectMapper objectMapper;

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;

	PactStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.objectMapper = new ObjectMapper();
		this.deleteStubsAfterTest = stubRunnerOptions.isDeleteStubsAfterTest();
		registerShutdownHook();
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		String version = stubConfiguration.version;
		// TODO: Read from stubrunner props or system props
		final SystemPropertyResolver resolver = new SystemPropertyResolver();
		List<String> tags = tags(stubConfiguration, version, resolver);
		PactBrokerLoader loader = pactBrokerLoader(resolver, tags);
		try {
			String providerName = stubConfiguration.getGroupId() + ":" + stubConfiguration.getArtifactId();
			List<Pact> pacts = loader.load(providerName);
			if (pacts.isEmpty()) {
				log.warn("No pact definitions found for provider [" + providerName + "]");
				return null;
			}
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.createTempDir(TEMP_DIR_PREFIX);
			for (int i = 0; i < pacts.size(); i++) {
				String json = toJson(pacts.get(i).toMap(PactSpecVersion.V3));
				File file = new File(tmpDirWhereStubsWillBeUnzipped,
						providerName.replace(":", "_") + "_pact_" + i + ".json");
				Files.write(file.toPath(), json.getBytes());
			}
			return new AbstractMap.SimpleEntry<>(stubConfiguration, tmpDirWhereStubsWillBeUnzipped);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@NotNull private PactBrokerLoader pactBrokerLoader(SystemPropertyResolver resolver,
			List<String> tags) {
		return new PactBrokerLoader(new PactBroker() {

			@Override public Class<? extends Annotation> annotationType() {
				return PactBroker.class;
			}

			@Override public String host() {
				return resolver.resolveValue("${pactbroker.host:}");
			}

			@Override public String port() {
				return resolver.resolveValue("${pactbroker.port:}");
			}

			@Override public String protocol() {
				return resolver.resolveValue("${pactbroker.protocol:http}");
			}

			@Override public String[] tags() {
				return tags.toArray(new String[0]);
			}

			@Override public boolean failIfNoPactsFound() {
				return true;
			}

			@Override public PactBrokerAuth authentication() {
				return new PactBrokerAuth() {
					@Override public Class<? extends Annotation> annotationType() {
						return PactBrokerAuth.class;
					}

					@Override public String scheme() {
						return resolver.resolveValue("${pactbroker.auth.scheme:basic}");
					}

					@Override public String username() {
						return resolver.resolveValue("${pactbroker.auth.username:}");
					}

					@Override public String password() {
						return resolver.resolveValue("${pactbroker.auth.password:}");
					}
				};
			}

			@Override public Class<? extends ValueResolver> valueResolver() {
				return SystemPropertyResolver.class;
			}
		});
	}

	@NotNull private List<String> tags(StubConfiguration stubConfiguration,
			String version, SystemPropertyResolver resolver) {
		String defaultTag = StubConfiguration.DEFAULT_VERSION.equals(version)
				? "latest" : version;
		return new ArrayList<>(Arrays.asList(StringUtils
				.commaDelimitedListToStringArray(
						resolver.resolveValue("${pactbroker.tags:" + defaultTag + "}"))));
	}

	private String toJson(Map map) {
		try {
			return this.objectMapper.writeValueAsString(map);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(
				() -> TemporaryFileStorage.cleanup(PactStubDownloader.this.deleteStubsAfterTest)));
	}
}