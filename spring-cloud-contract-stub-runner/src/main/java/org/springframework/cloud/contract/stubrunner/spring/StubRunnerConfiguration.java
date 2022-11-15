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

package org.springframework.cloud.contract.stubrunner.spring;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
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
 * @author Eddú Meléndez
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StubRunnerProperties.class)
@ConditionalOnMissingBean(type = "org.springframework.cloud.contract.wiremock.WiremockServerConfiguration")
@Import(StubRunnerPortBeanPostProcessor.class)
public class StubRunnerConfiguration {

	static final String STUBRUNNER_PREFIX = "stubrunner.runningstubs";

	private StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider();

	@Autowired
	private ConfigurableEnvironment environment;

	/**
	 * Bean that initializes stub runners, runs them and on shutdown closes them. Upon its
	 * instantiation JAR with stubs is downloaded and unpacked to a temporary folder and
	 * WireMock server are started for each of those stubs
	 * @param beanFactory bean factory
	 * @return the batch stub runner bean
	 */
	@Bean
	public BatchStubRunner batchStubRunner(BeanFactory beanFactory) {
		StubRunnerProperties props = beanFactory.getBean(StubRunnerProperties.class);
		StubRunnerOptionsBuilder builder = builder(props);
		if (props.getProxyHost() != null) {
			builder.withProxy(props.getProxyHost(), props.getProxyPort());
		}
		StubRunnerOptions stubRunnerOptions = stubRunnerOptions(builder);
		BatchStubRunner batchStubRunner = new BatchStubRunnerFactory(stubRunnerOptions,
				this.provider.get(stubRunnerOptions), new LazyMessageVerifier(beanFactory)).buildBatchStubRunner();
		// TODO: Consider running it in a separate thread
		RunningStubs runningStubs = batchStubRunner.runStubs();
		registerPort(runningStubs);
		return batchStubRunner;
	}

	private StubRunnerOptions stubRunnerOptions(StubRunnerOptionsBuilder builder) {
		return builder.build();
	}

	@Bean
	public BeanPostProcessor batchStubRunnerBeanPostProcessor(BatchStubRunner runner) {
		return new BeanPostProcessor() {
		};
	}

	private StubRunnerOptionsBuilder builder(StubRunnerProperties props) {
		return new StubRunnerOptionsBuilder()
				.withMinMaxPort(Integer.valueOf(resolvePlaceholder(props.getMinPort(), props.getMinPort())),
						Integer.valueOf(resolvePlaceholder(props.getMaxPort(), props.getMaxPort())))
				.withStubRepositoryRoot(props.getRepositoryRoot())
				.withStubsMode(resolvePlaceholder(props.getStubsMode()))
				.withStubsClassifier(resolvePlaceholder(props.getClassifier()))
				.withStubs(resolvePlaceholder(props.getIds())).withUsername(resolvePlaceholder(props.getUsername()))
				.withPassword(resolvePlaceholder(props.getPassword()))
				.withStubPerConsumer(Boolean.parseBoolean(resolvePlaceholder(props.isStubsPerConsumer())))
				.withConsumerName(consumerName(props))
				.withMappingsOutputFolder(resolvePlaceholder(props.getMappingsOutputFolder()))
				.withDeleteStubsAfterTest(Boolean.parseBoolean(resolvePlaceholder(props.isDeleteStubsAfterTest())))
				.withGenerateStubs(Boolean.parseBoolean(resolvePlaceholder(props.isGenerateStubs())))
				.withProperties(props.getProperties()).withHttpServerStubConfigurer(props.getHttpServerStubConfigurer())
				.withServerId(resolvePlaceholder(props.getServerId()));
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

	private String consumerName(StubRunnerProperties props) {
		if (StringUtils.hasText(props.getConsumerName())) {
			return resolvePlaceholder(props.getConsumerName());
		}
		return this.environment.getProperty("spring.application.name");
	}

	private void registerPort(RunningStubs runStubs) {
		MutablePropertySources propertySources = this.environment.getPropertySources();
		if (!propertySources.contains(STUBRUNNER_PREFIX)) {
			propertySources.addFirst(new MapPropertySource(STUBRUNNER_PREFIX, new HashMap<>()));
		}
		Map<String, Object> source = ((MapPropertySource) propertySources.get(STUBRUNNER_PREFIX)).getSource();
		for (Map.Entry<StubConfiguration, Integer> entry : runStubs.validNamesAndPorts().entrySet()) {
			source.put(STUBRUNNER_PREFIX + "." + entry.getKey().getArtifactId() + ".port", entry.getValue());
			// there are projects where artifact id is the same, what differs is the group
			// id
			source.put(STUBRUNNER_PREFIX + "." + entry.getKey().getGroupId() + "." + entry.getKey().getArtifactId()
					+ ".port", entry.getValue());
		}
	}

}

@SuppressWarnings("unchecked")
class LazyMessageVerifier implements MessageVerifier {

	private MessageVerifierSender<?> messageVerifierSender;

	private MessageVerifierReceiver<?> messageVerifierReceiver;

	private final BeanFactory beanFactory;

	LazyMessageVerifier(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	private MessageVerifierSender messageVerifierSender() {
		if (this.messageVerifierSender == null) {
			this.messageVerifierSender = this.beanFactory.getBeanProvider(MessageVerifierSender.class)
					.getIfAvailable(NoOpStubMessages::new);
		}
		return this.messageVerifierSender;
	}

	private MessageVerifierReceiver messageVerifierReceiver() {
		if (this.messageVerifierReceiver == null) {
			this.messageVerifierReceiver = this.beanFactory.getBeanProvider(MessageVerifierReceiver.class)
					.getIfAvailable(NoOpStubMessages::new);
		}
		return this.messageVerifierReceiver;
	}

	@Override
	public void send(Object message, String destination, YamlContract contract) {
		messageVerifierSender().send(message, destination, contract);
	}

	@Override
	public Object receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
		return messageVerifierReceiver().receive(destination, timeout, timeUnit, contract);
	}

	@Override
	public Object receive(String destination, YamlContract contract) {
		return messageVerifierReceiver().receive(destination, contract);
	}

	@Override
	public void send(Object payload, Map headers, String destination, YamlContract contract) {
		messageVerifierSender().send(payload, headers, destination, contract);
	}

}
