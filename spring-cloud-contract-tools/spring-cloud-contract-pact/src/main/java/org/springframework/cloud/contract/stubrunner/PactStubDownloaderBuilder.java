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
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.support.expressions.SystemPropertyResolver;
import au.com.dius.pact.core.support.expressions.ValueResolver;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerLoader;
import au.com.dius.pact.provider.junitsupport.loader.PactLoader;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.spec.pact.PactContractConverter;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Allows downloading of Pact files from the Pact Broker.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.0.0
 */
public final class PactStubDownloaderBuilder implements StubDownloaderBuilder {

	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections.singletonList("pact");

	/**
	 * Does any of the accepted protocols matches the URL of the repository.
	 * @param url - of the repository
	 * @return {@code true} if the protocol is accepted
	 */
	private static boolean isProtocolAccepted(String url) {
		return ACCEPTABLE_PROTOCOLS.stream().anyMatch(url::startsWith);
	}

	@Override
	public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.getStubsMode() == StubRunnerProperties.StubsMode.CLASSPATH
				|| stubRunnerOptions.getStubRepositoryRoot() == null) {
			return null;
		}
		Resource resource = stubRunnerOptions.getStubRepositoryRoot();
		if (!(resource instanceof PactResource)) {
			return null;
		}
		return new PactStubDownloader(stubRunnerOptions);
	}

	@Override
	public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!StringUtils.hasText(location) || !isProtocolAccepted(location)) {
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

	@Override
	public String getDescription() {
		return this.rawLocation;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public URI getURI() {
		return URI.create(this.rawLocation);
	}

}

class PactStubDownloader implements StubDownloader {

	private static final String TEMP_DIR_PREFIX = "pact";

	private static final Log log = LogFactory.getLog(PactStubDownloader.class);

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;

	private static final String ARTIFICIAL_NAME_ENDING_WITH_GROOVY = "name.groovy";

	private static final String PROVIDER_NAME_WITH_GROUP_ID = "pactbroker.provider-name-with-group-id";

	private final StubRunnerOptions stubRunnerOptions;

	private final boolean deleteStubsAfterTest;

	private final ObjectMapper objectMapper;

	PactStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.objectMapper = new ObjectMapper();
		this.deleteStubsAfterTest = stubRunnerOptions.isDeleteStubsAfterTest();
		registerShutdownHook();
	}

	@Override
	public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(StubConfiguration stubConfiguration) {
		String version = stubConfiguration.version;
		final FromPropsThenFromSysEnv resolver = new FromPropsThenFromSysEnv(this.stubRunnerOptions);
		List<String> tags = tags(version, resolver);
		try {
			PactLoader loader = pactBrokerLoader(resolver, tags);
			String providerName = providerName(stubConfiguration);
			List<Pact> pacts = loader.load(providerName);
			if (pacts.isEmpty()) {
				if (log.isWarnEnabled()) {
					log.warn("No pact definitions found for provider [" + providerName + "]");
				}
				return null;
			}
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.createTempDir(TEMP_DIR_PREFIX);
			// make the groupid / artifactid folders
			String coordinatesFolderName = stubConfiguration.getGroupId().replace(".", File.separator) + File.separator
					+ stubConfiguration.getArtifactId();
			File contractsFolder = new File(tmpDirWhereStubsWillBeUnzipped,
					coordinatesFolderName + File.separator + "contracts");
			File mappingsFolder = new File(tmpDirWhereStubsWillBeUnzipped,
					coordinatesFolderName + File.separator + "mappings");
			boolean createdContractsDirs = contractsFolder.mkdirs();
			boolean createdMappingsDirs = mappingsFolder.mkdirs();
			if (!createdContractsDirs || !createdMappingsDirs) {
				throw new IllegalStateException("Failed to create mandatory [contracts] or [mappings] folders under ["
						+ coordinatesFolderName + "]");
			}
			storePacts(providerName, pacts, contractsFolder, mappingsFolder);
			return new AbstractMap.SimpleEntry<>(stubConfiguration, tmpDirWhereStubsWillBeUnzipped);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void storePacts(String providerName, List<Pact> pacts, File contractsFolder, File mappingsFolder) {
		for (int i = 0; i < pacts.size(); i++) {
			String json = toJson(pacts.get(i).toMap(PactSpecVersion.V3));
			File file = new File(contractsFolder, i + "_" + providerName.replace(":", "_") + "_pact.json");
			storeFile(file.toPath(), json.getBytes());
			try {
				storeMapping(mappingsFolder, file);
			}
			catch (Exception e) {
				log.warn("Exception occurred while trying to store the mapping", e);
			}
		}
	}

	private void storeMapping(File mappingsFolder, File file) {
		Collection<Contract> contracts = new PactContractConverter().convertFrom(file);
		if (log.isDebugEnabled()) {
			log.debug("Converted pact file [" + file + "] to [" + contracts.size() + "] contracts");
		}
		MappingGenerator.toMappings(file, contracts, mappingsFolder);
	}

	private Path storeFile(Path path, byte[] contents) {
		try {
			Path storedPath = Files.write(path, contents);
			if (log.isDebugEnabled()) {
				log.debug("Stored file [" + path + "]");
			}
			return storedPath;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String providerName(StubConfiguration stubConfiguration) {
		boolean providerNameWithGroupId = Boolean.parseBoolean(StubRunnerPropertyUtils
				.getProperty(this.stubRunnerOptions.getProperties(), PROVIDER_NAME_WITH_GROUP_ID));
		if (providerNameWithGroupId) {
			return stubConfiguration.getGroupId() + ":" + stubConfiguration.getArtifactId();
		}
		return stubConfiguration.getArtifactId();
	}

	PactLoader pactBrokerLoader(ValueResolver resolver, List<String> tags) throws IOException {
		Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
		String schemeSpecificPart = schemeSpecificPart(repo.getURI());
		URI pactBrokerUrl = URI.create(schemeSpecificPart);
		String stubRunnerUsername = this.stubRunnerOptions.getUsername() != null ? this.stubRunnerOptions.getUsername()
				: "";
		String stubRunnerPassword = this.stubRunnerOptions.getPassword() != null ? this.stubRunnerOptions.getPassword()
				: "";
		return new PactBrokerLoader(new PactBroker() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return PactBroker.class;
			}

			@Override
			public String url() {
				return resolver.resolveValue("pactbroker.url",
						pactBrokerUrl.getScheme() + "://" + pactBrokerUrl.getHost() + ":" + pactBrokerUrl.getPort());
			}

			@Override
			@Deprecated
			public String host() {
				return resolver.resolveValue("pactbroker.host", pactBrokerUrl.getHost());
			}

			@Override
			@Deprecated
			public String port() {
				return resolver.resolveValue("pactbroker.port", String.valueOf(pactBrokerUrl.getPort()));
			}

			@Override
			@Deprecated
			public String scheme() {
				return resolver.resolveValue("pactbroker.protocol", pactBrokerUrl.getScheme());
			}

			@Override
			@Deprecated
			public String[] tags() {
				return new String[0];
			}

			@Override
			public VersionSelector[] consumerVersionSelectors() {
				return new VersionSelector[] { new VersionSelector() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return VersionSelector.class;
					}

					@Override
					public String tag() {
						return resolver.resolveValue("pactbroker.consumerversionselectors.tags", "");
					}

					@Override
					public String latest() {
						return resolver.resolveValue("pactbroker.consumerversionselectors.latest", "true");
					}

					@Override
					public String consumer() {
						return resolver.resolveValue("pactbroker.consumers", "");
					}

					@Override
					public String fallbackTag() {
						return resolver.resolveValue("pactbroker.fallbacktag", "");
					}
				} };
			}

			@Override
			public String[] consumers() {
				return new String[] { resolver.resolveValue("pactbroker.consumers", "") };
			}

			@Override
			public PactBrokerAuth authentication() {
				return new PactBrokerAuth() {
					@Override
					public Class<? extends Annotation> annotationType() {
						return PactBrokerAuth.class;
					}

					@Override
					public String username() {
						return resolver.resolveValue("pactbroker.auth.username", stubRunnerUsername);
					}

					@Override
					public String password() {
						return resolver.resolveValue("pactbroker.auth.password", stubRunnerPassword);
					}

					@Override
					public String token() {
						return resolver.resolveValue("pactbroker.auth.token", "");
					}
				};
			}

			@Override
			public Class<? extends ValueResolver> valueResolver() {
				return SystemPropertyResolver.class;
			}

			@Override
			public String enablePendingPacts() {
				return resolver.resolveValue("pactbroker.enablePending", "false");
			}

			@Override
			public String[] providerTags() {
				return resolver.resolveValue("pactbroker.providerTags", "").split(",");
			}

			@Override
			public String providerBranch() {
				return resolver.resolveValue("pactbroker.providerBranch", "");
			}

			@Override
			public String includeWipPactsSince() {
				return resolver.resolveValue("pactbroker.includeWipPactsSince", "");
			}

			@Override
			public String enableInsecureTls() {
				return resolver.resolveValue("pactbroker.enableInsecureTls", "false");
			}
		});
	}

	private String schemeSpecificPart(URI uri) {
		String part = uri.getSchemeSpecificPart();
		if (!StringUtils.hasText(part)) {
			return part;
		}
		return part.startsWith("//") ? part.substring(2) : part;
	}

	private List<String> tags(String version, ValueResolver resolver) {
		String defaultTag = StubConfiguration.DEFAULT_VERSION.equals(version) ? "latest" : version;
		return new ArrayList<>(Arrays.asList(
				StringUtils.commaDelimitedListToStringArray(resolver.resolveValue("pactbroker.tags", defaultTag))));
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
		Runtime.getRuntime().addShutdownHook(
				new Thread(() -> TemporaryFileStorage.cleanup(PactStubDownloader.this.deleteStubsAfterTest)));
	}

}

class FromPropsThenFromSysEnv implements ValueResolver {

	final SystemPropertyResolver resolver = SystemPropertyResolver.INSTANCE;

	StubRunnerOptions options;

	FromPropsThenFromSysEnv(StubRunnerOptions options) {
		this.options = options;
	}

	@Override
	public String resolveValue(String expression) {
		return doResolve(expression, null);
	}

	@Nullable
	private String doResolve(String expression, @Nullable String defaultValue) {
		PropertyValueTuple tuple = new PropertyValueTuple(expression).invoke();
		String propertyName = tuple.getPropertyName();
		String property = StubRunnerPropertyUtils.getProperty(this.options.getProperties(), propertyName);
		if (StringUtils.hasText(property)) {
			return property;
		}
		return this.resolver.resolveValue(expression, defaultValue);
	}

	@Override
	public boolean propertyDefined(String property) {
		PropertyValueTuple tuple = new PropertyValueTuple(property).invoke();
		String propertyName = tuple.getPropertyName();
		boolean hasProperty = StubRunnerPropertyUtils.hasProperty(this.options.getProperties(), propertyName);
		if (hasProperty) {
			return true;
		}
		return this.resolver.propertyDefined(property);
	}

	@Nullable
	@Override
	public String resolveValue(String expression, String defaultValue) {
		return doResolve(expression, defaultValue);
	}

}

// taken from pact -
// au.com.dius.pact.provider.junit.sysprops.SystemPropertyResolver.PropertyValueTuple
class PropertyValueTuple {

	private String propertyName;

	PropertyValueTuple(String property) {
		this.propertyName = property;
	}

	String getPropertyName() {
		return this.propertyName;
	}

	PropertyValueTuple invoke() {
		if (this.propertyName.contains(":")) {
			String[] kv = org.apache.commons.lang3.StringUtils.splitPreserveAllTokens(this.propertyName, ':');
			this.propertyName = kv[0];
		}
		return this;
	}

}
