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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
@ConditionalOnMissingBean(type = "org.springframework.cloud.contract.wiremock.WiremockServerConfiguration")
@Import(StubRunnerPortBeanPostProcessor.class)
public class StubRunnerConfiguration {

	static final String STUBRUNNER_PREFIX = "stubrunner.runningstubs";

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
	public BatchStubRunner batchStubRunner(BeanFactory beanFactory) {
		StubRunnerOptionsBuilder builder = builder();
		if (this.props.getProxyHost() != null) {
			builder.withProxy(this.props.getProxyHost(), this.props.getProxyPort());
		}
		StubRunnerOptions stubRunnerOptions = builder.build();
		BatchStubRunner batchStubRunner = new BatchStubRunnerFactory(stubRunnerOptions,
				this.provider.get(stubRunnerOptions),
				new LazyMessageVerifier(beanFactory)).buildBatchStubRunner();
		// TODO: Consider running it in a separate thread
		RunningStubs runningStubs = batchStubRunner.runStubs();
		registerPort(runningStubs);
		return batchStubRunner;
	}

	@Bean
	public BeanPostProcessor batchStubRunnerBeanPostProcessor(BatchStubRunner runner) {
		return new BeanPostProcessor() {
		};
	}

	private StubRunnerOptionsBuilder builder() {
		return new StubRunnerOptionsBuilder()
				.withMinMaxPort(this.props.getMinPort(), this.props.getMaxPort())
				.withStubRepositoryRoot(this.props.getRepositoryRoot())
				.withStubsMode(this.props.getStubsMode())
				.withStubsClassifier(this.props.getClassifier())
				.withStubs(this.props.getIds()).withUsername(this.props.getUsername())
				.withPassword(this.props.getPassword())
				.withStubPerConsumer(this.props.isStubsPerConsumer())
				.withConsumerName(consumerName())
				.withMappingsOutputFolder(this.props.getMappingsOutputFolder())
				.withDeleteStubsAfterTest(this.props.isDeleteStubsAfterTest())
				.withProperties(this.props.getProperties())
				.withHttpServerStubConfigurer(this.props.getHttpServerStubConfigurer());
	}

	private String consumerName() {
		if (StringUtils.hasText(this.props.getConsumerName())) {
			return this.props.getConsumerName();
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

class LazyMessageVerifier implements MessageVerifier {

	private MessageVerifier<?> messageVerifier;

	private final BeanFactory beanFactory;

	LazyMessageVerifier(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	private MessageVerifier messageVerifier() {
		if (this.messageVerifier == null) {
			try {
				this.messageVerifier = this.beanFactory.getBean(MessageVerifier.class);
			}
			catch (BeansException ex) {
				this.messageVerifier = new NoOpStubMessages();
			}
		}
		return this.messageVerifier;
	}

	@Override
	public void send(Object message, String destination) {
		messageVerifier().send(message, destination);
	}

	@Override
	public Object receive(String destination, long timeout, TimeUnit timeUnit) {
		return messageVerifier().receive(destination, timeout, timeUnit);
	}

	@Override
	public Object receive(String destination) {
		return messageVerifier().receive(destination);
	}

	@Override
	public void send(Object payload, Map headers, String destination) {
		messageVerifier().send(payload, headers, destination);
	}

}