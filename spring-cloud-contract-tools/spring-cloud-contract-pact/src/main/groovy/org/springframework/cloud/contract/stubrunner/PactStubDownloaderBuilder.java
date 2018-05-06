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
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.model.PactSpecVersion;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.loader.PactBrokerLoader;
import au.com.dius.pact.provider.junit.loader.PactLoader;
import au.com.dius.pact.provider.junit.sysprops.SystemPropertyResolver;
import au.com.dius.pact.provider.junit.sysprops.ValueResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.converter.StubGenerator;
import org.springframework.cloud.contract.verifier.converter.StubGeneratorProvider;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;
import org.springframework.cloud.contract.verifier.spec.pact.PactContractConverter;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Allows downloading of Pact files from the Pact Broker.
 *
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.0.0
 */
public final class PactStubDownloaderBuilder implements StubDownloaderBuilder {
	private static final List<String> ACCEPTABLE_PROTOCOLS = Collections
			.singletonList("pact");

	/**
	 * Does any of the accepted protocols matches the URL of the repository
	 * @param url - of the repository
	 */
	private static boolean isProtocolAccepted(String url) {
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

	@Override public InputStream getInputStream() {
		return null;
	}

	@Override public URI getURI() {
		return URI.create(this.rawLocation);
	}
}

class PactStubDownloader implements StubDownloader {
	private static final String TEMP_DIR_PREFIX = "pact";
	private static final Log log = LogFactory.getLog(PactStubDownloader.class);

	// Preloading class for the shutdown hook not to throw ClassNotFound
	private static final Class CLAZZ = TemporaryFileStorage.class;
	private static final String ARTIFICIAL_NAME_ENDING_WITH_GROOVY = "name.groovy";

	private final StubRunnerOptions stubRunnerOptions;
	private final boolean deleteStubsAfterTest;
	private final ObjectMapper objectMapper;

	private static final String PROVIDER_NAME_WITH_GROUP_ID = "pactbroker.provider-name-with-group-id";

	PactStubDownloader(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		this.objectMapper = new ObjectMapper();
		this.deleteStubsAfterTest = stubRunnerOptions.isDeleteStubsAfterTest();
		registerShutdownHook();
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		String version = stubConfiguration.version;
		final FromPropsThenFromSysEnv resolver = new FromPropsThenFromSysEnv(this.stubRunnerOptions);
		List<String> tags = tags(version, resolver);
		try {
			PactLoader loader = pactBrokerLoader(resolver, tags);
			String providerName = providerName(stubConfiguration);
			List<Pact> pacts = loader.load(providerName);
			if (pacts.isEmpty()) {
				log.warn("No pact definitions found for provider [" + providerName + "]");
				return null;
			}
			File tmpDirWhereStubsWillBeUnzipped = TemporaryFileStorage.createTempDir(TEMP_DIR_PREFIX);
			// make the groupid / artifactid folders
			String coordinatesFolderName = stubConfiguration.getGroupId().replace(".", File.separator) +
					File.separator + stubConfiguration.getArtifactId();
			File contractsFolder = new File(tmpDirWhereStubsWillBeUnzipped,
					coordinatesFolderName + File.separator + "contracts");
			File mappingsFolder = new File(tmpDirWhereStubsWillBeUnzipped,
					coordinatesFolderName + File.separator + "mappings");
			boolean createdContractsDirs = contractsFolder.mkdirs();
			boolean createdMappingsDirs = mappingsFolder.mkdirs();
			if (!createdContractsDirs || !createdMappingsDirs) {
				throw new IllegalStateException("Failed to create mandatory [contracts] or [mappings] folders under [" + coordinatesFolderName + "]");
			}
			storePacts(providerName, pacts, contractsFolder, mappingsFolder);
			return new AbstractMap.SimpleEntry<>(stubConfiguration, tmpDirWhereStubsWillBeUnzipped);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void storePacts(String providerName, List<Pact> pacts, File contractsFolder,
			File mappingsFolder) {
		for (int i = 0; i < pacts.size(); i++) {
			String json = toJson(pacts.get(i).toMap(PactSpecVersion.V3));
			File file = new File(contractsFolder, i + "_" +
					providerName.replace(":", "_") + "_pact.json");
			storeFile(file.toPath(), json.getBytes());
			try {
				storeMapping(mappingsFolder, file);
			} catch (Exception e) {
				log.warn("Exception occurred while trying to store the mapping", e);
			}
		}
	}

	private void storeMapping(File mappingsFolder, File file) {
		Collection<Contract> contracts = new PactContractConverter()
				.convertFrom(file);
		if (log.isDebugEnabled()) {
			log.debug("Converted pact file [" + file + "] to [" + contracts.size() + "] contracts");
		}
		StubGeneratorProvider provider = new StubGeneratorProvider();
		Collection<StubGenerator> stubGenerators = provider
				.converterForName(ARTIFICIAL_NAME_ENDING_WITH_GROOVY);
		if (log.isDebugEnabled()) {
			log.debug("Found following matching stub generators " + stubGenerators);
		}
		for (StubGenerator stubGenerator : stubGenerators) {
			Map<Contract, String> map = stubGenerator
					.convertContents(file.getName(),
							new ContractMetadata(file.toPath(), false,
									contracts.size(), null, contracts));
			for (Map.Entry<Contract, String> entry : map.entrySet()) {
				String value = entry.getValue();
				File mapping = new File(mappingsFolder,
						StringUtils.stripFilenameExtension(file.getName()) + "_" +
								Math.abs(entry.getKey().hashCode()) + ".json");
				storeFile(mapping.toPath(), value.getBytes());
			}
		}
	}

	private void storeFile(Path path, byte[] contents) {
		try {
			Files.write(path, contents);
			if (log.isDebugEnabled()) {
				log.debug("Stored file [" + path.toString() + "]");
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String providerName(StubConfiguration stubConfiguration) {
		boolean providerNameWithGroupId = Boolean.parseBoolean(
				StubRunnerPropertyUtils.getProperty(this.stubRunnerOptions.getProperties(),
				PROVIDER_NAME_WITH_GROUP_ID));
		if (providerNameWithGroupId) {
			return stubConfiguration.getGroupId() + ":" + stubConfiguration.getArtifactId();
		}
		return stubConfiguration.getArtifactId();
	}

	@NotNull PactLoader pactBrokerLoader(ValueResolver resolver,
			List<String> tags) throws IOException {
		Resource repo = this.stubRunnerOptions.getStubRepositoryRoot();
		String schemeSpecificPart = schemeSpecificPart(repo.getURI());
		URI pactBrokerUrl = URI.create(schemeSpecificPart);
		String stubRunnerUsername = this.stubRunnerOptions.getUsername();
		String stubRunnerPassword = this.stubRunnerOptions.getPassword();
		return new PactBrokerLoader(new PactBroker() {

			@Override public Class<? extends Annotation> annotationType() {
				return PactBroker.class;
			}

			@Override public String host() {
				return resolver.resolveValue("pactbroker.host:" + pactBrokerUrl.getHost());
			}

			@Override public String port() {
				return resolver.resolveValue("pactbroker.port:" + pactBrokerUrl.getPort());
			}

			@Override public String protocol() {
				return resolver.resolveValue("pactbroker.protocol:" + pactBrokerUrl.getScheme());
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
						return resolver.resolveValue("pactbroker.auth.scheme:Basic");
					}

					@Override public String username() {
						return resolver.resolveValue("pactbroker.auth.username:" + stubRunnerUsername);
					}

					@Override public String password() {
						return resolver.resolveValue("pactbroker.auth.password:" + stubRunnerPassword);
					}
				};
			}

			@Override public Class<? extends ValueResolver> valueResolver() {
				return SystemPropertyResolver.class;
			}
		});
	}

	private String schemeSpecificPart(URI uri) {
		String part = uri.getSchemeSpecificPart();
		if (StringUtils.isEmpty(part)) {
			return part;
		}
		return part.startsWith("//") ? part.substring(2) : part;
	}

	@NotNull private List<String> tags(String version, ValueResolver resolver) {
		String defaultTag = StubConfiguration.DEFAULT_VERSION.equals(version)
				? "latest" : version;
		return new ArrayList<>(Arrays.asList(StringUtils
				.commaDelimitedListToStringArray(
						resolver.resolveValue("pactbroker.tags:" + defaultTag + ""))));
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

class FromPropsThenFromSysEnv implements ValueResolver {

	final SystemPropertyResolver resolver = new SystemPropertyResolver();
	StubRunnerOptions options;

	FromPropsThenFromSysEnv(StubRunnerOptions options) {
		this.options = options;
	}

	@Override public String resolveValue(String expression) {
		PropertyValueTuple tuple = new PropertyValueTuple(expression).invoke();
		String propertyName = tuple.getPropertyName();
		String property = StubRunnerPropertyUtils
				.getProperty(this.options.getProperties(), propertyName);
		if (StringUtils.hasText(property)) {
			return property;
		}
		return this.resolver.resolveValue(expression);
	}

	@Override public boolean propertyDefined(String property) {
		PropertyValueTuple tuple = new PropertyValueTuple(property).invoke();
		String propertyName = tuple.getPropertyName();
		boolean hasProperty = StubRunnerPropertyUtils
				.hasProperty(this.options.getProperties(), propertyName);
		if (hasProperty) {
			return true;
		}
		return this.resolver.propertyDefined(property);
	}
}

// taken from pact - au.com.dius.pact.provider.junit.sysprops.SystemPropertyResolver.PropertyValueTuple
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
			String[] kv = org.apache.commons.lang3.StringUtils
					.splitPreserveAllTokens(this.propertyName, ':');
			this.propertyName = kv[0];
		}
		return this;
	}
}