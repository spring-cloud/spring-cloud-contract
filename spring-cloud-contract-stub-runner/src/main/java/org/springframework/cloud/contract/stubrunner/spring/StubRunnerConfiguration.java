/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilderProvider;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Configuration that initializes a {@link BatchStubRunner} that runs
 * {@link org.springframework.cloud.contract.stubrunner.StubRunner} instance for each
 * stub.
 *
 * @author Marcin Grzejszczak
 */
@Configuration
@EnableConfigurationProperties(StubRunnerProperties.class)
@ConditionalOnMissingBean(
		type = "org.springframework.cloud.contract.wiremock.WiremockServerConfiguration")
@Import(StubRunnerPortBeanPostProcessor.class)
public class StubRunnerConfiguration {

	static final String STUBRUNNER_PREFIX = "stubrunner.runningstubs";

	@Autowired(required = false)
	private MessageVerifier<?> contractVerifierMessaging;

	private StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider();

	@Autowired
	private StubRunnerProperties props;

	@Autowired
	private ConfigurableEnvironment environment;

	/**
	 * Bean that initializes stub runners, runs them and on shutdown closes them. Upon its
	 * instantiation JAR with stubs is downloaded and unpacked to a temporary folder and
	 * WireMock server are started for each of those stubs
	 * @return the batch stub runner bean
	 */
	@Bean
	public BatchStubRunner batchStubRunner() {
		StubRunnerOptionsBuilder builder = builder();
		if (this.props.getProxyHost() != null) {
			builder.withProxy(this.props.getProxyHost(), this.props.getProxyPort());
		}
		StubRunnerOptions stubRunnerOptions = stubRunnerOptions(builder);
		BatchStubRunner batchStubRunner = new BatchStubRunnerFactory(stubRunnerOptions,
				this.provider.get(stubRunnerOptions),
				this.contractVerifierMessaging != null ? this.contractVerifierMessaging
						: new NoOpStubMessages()).buildBatchStubRunner();
		// TODO: Consider running it in a separate thread
		RunningStubs runningStubs = batchStubRunner.runStubs();
		registerPort(runningStubs);
		return batchStubRunner;
	}

	private StubRunnerOptions stubRunnerOptions(StubRunnerOptionsBuilder builder) {
		return builder.build();
	}

	private StubRunnerOptionsBuilder builder() {
		return new StubRunnerOptionsBuilder()
				.withMinMaxPort(
						Integer.valueOf(resolvePlaceholder(this.props.getMinPort(),
								this.props.getMinPort())),
						Integer.valueOf(resolvePlaceholder(this.props.getMaxPort(),
								this.props.getMaxPort())))
				.withStubRepositoryRoot(this.props.getRepositoryRoot())
				.withStubsMode(resolvePlaceholder(this.props.getStubsMode()))
				.withStubsClassifier(resolvePlaceholder(this.props.getClassifier()))
				.withStubs(resolvePlaceholder(this.props.getIds()))
				.withUsername(resolvePlaceholder(this.props.getUsername()))
				.withPassword(resolvePlaceholder(this.props.getPassword()))
				.withStubPerConsumer(Boolean.parseBoolean(
						resolvePlaceholder(this.props.isStubsPerConsumer())))
				.withConsumerName(consumerName())
				.withMappingsOutputFolder(
						resolvePlaceholder(this.props.getMappingsOutputFolder()))
				.withDeleteStubsAfterTest(Boolean.parseBoolean(
						resolvePlaceholder(this.props.isDeleteStubsAfterTest())))
				.withGenerateStubs(Boolean
						.parseBoolean(resolvePlaceholder(this.props.isGenerateStubs())))
				.withProperties(this.props.getProperties())
				.withHttpServerStubConfigurer(this.props.getHttpServerStubConfigurer());
	}

	private String[] resolvePlaceholder(String[] string) {
		return Arrays.stream(string).map(this::resolvePlaceholder).toArray(String[]::new);
	}

	private String resolvePlaceholder(Object string) {
		return resolvePlaceholder(string, null);
	}

	private String resolvePlaceholder(Object string, Object defaultValue) {
		if (string == null) {
			return defaultValue != null ? defaultValue.toString() : null;
		}
		return this.environment.resolvePlaceholders(string.toString());
	}

	private String consumerName() {
		if (StringUtils.hasText(this.props.getConsumerName())) {
			return resolvePlaceholder(this.props.getConsumerName());
		}
		return this.environment.getProperty("spring.application.name");
	}

	private void registerPort(RunningStubs runStubs) {
		MutablePropertySources propertySources = this.environment.getPropertySources();
		if (!propertySources.contains(STUBRUNNER_PREFIX)) {
			propertySources
					.addFirst(new MapPropertySource(STUBRUNNER_PREFIX, new HashMap<>()));
		}
		Map<String, Object> source = ((MapPropertySource) propertySources
				.get(STUBRUNNER_PREFIX)).getSource();
		for (Map.Entry<StubConfiguration, Integer> entry : runStubs.validNamesAndPorts()
				.entrySet()) {
			source.put(STUBRUNNER_PREFIX + "." + entry.getKey().getArtifactId() + ".port",
					entry.getValue());
			// there are projects where artifact id is the same, what differs is the group
			// id
			source.put(STUBRUNNER_PREFIX + "." + entry.getKey().getGroupId() + "."
					+ entry.getKey().getArtifactId() + ".port", entry.getValue());
		}
	}

}
