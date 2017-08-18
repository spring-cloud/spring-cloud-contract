/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.junit;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * JUnit class rule that allows you to download the provided stubs.
 *
 * @author Marcin Grzejszczak
 */
public class StubRunnerRule implements TestRule, StubFinder, StubRunnerRuleOptions {
	private static final String DELIMITER = ":";
	private static final String LATEST_VERSION = "+";

	StubRunnerOptionsBuilder stubRunnerOptionsBuilder = new StubRunnerOptionsBuilder(defaultStubRunnerOptions());
	BatchStubRunner stubFinder;
	MessageVerifier verifier = new ExceptionThrowingMessageVerifier();
	StubRunnerRule delegate = this;

	public StubRunnerRule() {
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				base.evaluate();
				StubRunnerRule.this.stubFinder().close();
			}

			private void before() {
				stubFinder(new BatchStubRunnerFactory(builder().build(), verifier()).buildBatchStubRunner());
				StubRunnerRule.this.stubFinder().runStubs();
			}
		};
	}

	private StubRunnerOptions defaultStubRunnerOptions() {
		StubRunnerOptionsBuilder builder = new StubRunnerOptionsBuilder()
				.withMinPort(Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000")))
				.withMaxPort(Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000")))
				.withStubRepositoryRoot(System.getProperty("stubrunner.repository.root", ""))
				.withWorkOffline(Boolean.parseBoolean(System.getProperty("stubrunner.work-offline", "false")))
				.withStubsClassifier(System.getProperty("stubrunner.classifier", "stubs"))
				.withStubs(System.getProperty("stubrunner.ids", ""))
				.withUsername(System.getProperty("stubrunner.username"))
				.withPassword(System.getProperty("stubrunner.password"))
				.withStubPerConsumer(Boolean.parseBoolean(System.getProperty("stubrunner.stubsPerConsumer", "false")))
				.withConsumerName(System.getProperty("stubrunner.consumer-name"));
		String proxyHost = System.getProperty("stubrunner.proxy.host");
		if (proxyHost != null) {
			builder.withProxy(proxyHost, Integer.parseInt(System.getProperty("stubrunner.proxy.port")));
		}
		return builder.build();
	}

	@Override public StubRunnerRule messageVerifier(MessageVerifier messageVerifier) {
		verifier(messageVerifier);
		return this.delegate;
	}

	@Override public StubRunnerRule options(StubRunnerOptions stubRunnerOptions) {
		builder().withOptions(stubRunnerOptions);
		return this.delegate;
	}

	@Override public StubRunnerRule minPort(int minPort) {
		builder().withMinPort(minPort);
		return this.delegate;
	}

	@Override public StubRunnerRule maxPort(int maxPort) {
		builder().withMaxPort(maxPort);
		return this.delegate;
	}

	@Override public StubRunnerRule repoRoot(String repoRoot) {
		builder().withStubRepositoryRoot(repoRoot);
		return this.delegate;
	}

	@Override public StubRunnerRule workOffline(boolean workOffline) {
		builder().withWorkOffline(workOffline);
		return this.delegate;
	}

	@Override public PortStubRunnerRule downloadStub(String groupId, String artifactId,
			String version, String classifier) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + version + DELIMITER + classifier);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public PortStubRunnerRule downloadLatestStub(String groupId, String artifactId,
			String classifier) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + LATEST_VERSION + DELIMITER + classifier);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public PortStubRunnerRule downloadStub(String groupId, String artifactId,
			String version) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + version);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public PortStubRunnerRule downloadStub(String groupId, String artifactId) {
		builder().withStubs(groupId + DELIMITER + artifactId);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public PortStubRunnerRule downloadStub(String ivyNotation) {
		builder().withStubs(ivyNotation);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public StubRunnerRule downloadStubs(String... ivyNotations) {
		builder().withStubs(Arrays.asList(ivyNotations));
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public StubRunnerRule downloadStubs(List<String> ivyNotations) {
		builder().withStubs(ivyNotations);
		return new PortStubRunnerRule(this.delegate);
	}

	@Override public StubRunnerRule withStubPerConsumer(boolean stubPerConsumer) {
		builder().withStubPerConsumer(stubPerConsumer);
		return this.delegate;
	}

	@Override public StubRunnerRule withConsumerName(String consumerName) {
		builder().withConsumerName(consumerName);
		return this.delegate;
	}

	@Override public StubRunnerRule withMappingsOutputFolder(String mappingsOutputFolder) {
		builder().withMappingsOutputFolder(mappingsOutputFolder);
		return this.delegate;
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		return this.stubFinder().findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		return this.stubFinder().findStubUrl(ivyNotation);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return this.stubFinder().findAllRunningStubs();
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return this.stubFinder().getContracts();
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		boolean result = this.stubFinder().trigger(ivyNotation, labelName);
		if (!result) {
			throw new IllegalStateException("Failed to trigger a message with notation [" + ivyNotation + "] and label [" + labelName + "]");
		}
		return result;
	}

	@Override
	public boolean trigger(String labelName) {
		boolean result = this.stubFinder().trigger(labelName);
		if (!result) {
			throw new IllegalStateException("Failed to trigger a message with label [" + labelName + "]");
		}
		return result;
	}

	@Override
	public boolean trigger() {
		boolean result = this.stubFinder().trigger();
		if (!result) {
			throw new IllegalStateException("Failed to trigger a message");
		}
		return result;
	}

	@Override
	public Map<String, Collection<String>> labels() {
		return this.stubFinder().labels();
	}

	StubRunnerRule(StubRunnerRule delegate) {
		this.delegate = delegate;
	}

	BatchStubRunner stubFinder() {
		return this.delegate.stubFinder;
	}

	void stubFinder(BatchStubRunner stubFinder) {
		this.delegate.stubFinder = stubFinder;
	}

	MessageVerifier verifier() {
		return this.delegate.verifier;
	}

	void verifier(MessageVerifier verifier) {
		this.delegate.verifier = verifier;
	}

	StubRunnerOptionsBuilder builder() {
		return this.delegate.stubRunnerOptionsBuilder;
	}

	static class ExceptionThrowingMessageVerifier implements MessageVerifier {

		private static final String EXCEPTION_MESSAGE = "Please provide a custom MessageVerifier to use this feature";

		@Override public void send(Object message, String destination) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}

		@Override public Object receive(String destination, long timeout,
				TimeUnit timeUnit) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}

		@Override public Object receive(String destination) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}

		@Override public void send(Object payload, Map headers, String destination) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}
	}

	/**
	 * Helper class with additional port, related methods once you pick a stub to download
	 *
	 * @since 1.2.0
	 */
	public static class PortStubRunnerRule extends StubRunnerRule implements PortStubRunnerRuleOptions {
		PortStubRunnerRule(StubRunnerRule delegate) {
			super(delegate);
		}

		@Override public StubRunnerRule withPort(Integer port) {
			builder().withPort(port);
			return this.delegate;
		}
	}

}
