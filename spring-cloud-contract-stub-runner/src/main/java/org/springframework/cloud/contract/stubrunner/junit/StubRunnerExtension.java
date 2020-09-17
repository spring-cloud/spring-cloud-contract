/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.cloud.contract.stubrunner.junit;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer;
import org.springframework.cloud.contract.stubrunner.RunningStubs;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.StubNotFoundException;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * JUnit 5 extension that allows to download and run stubs.
 *
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
public class StubRunnerExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback,
		StubFinder, StubRunnerExtensionOptions {

	private static final String DELIMITER = ":";

	private static final String LATEST_VERSION = "+";

	private static final Log LOG = LogFactory.getLog(StubRunnerExtension.class);

	StubRunnerExtension delegate = this;

	private AtomicBoolean beforeAllCalled = new AtomicBoolean();

	private AtomicBoolean afterAllCalled = new AtomicBoolean();

	private BatchStubRunner stubFinder;

	private StubRunnerOptionsBuilder stubRunnerOptionsBuilder = new StubRunnerOptionsBuilder(
			StubRunnerOptions.fromSystemProps());

	private MessageVerifier verifier = new ExceptionThrowingMessageVerifier();

	public StubRunnerExtension() {
	}

	StubRunnerExtension(StubRunnerExtension delegate) {
		this.delegate = delegate;
	}

	@Override
	public void afterAll(ExtensionContext extensionContext) {
		try {
			after();
		}
		finally {
			this.afterAllCalled.set(true);
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if (extensionOverStaticField()) {
			return;
		}
		after();
	}

	private boolean extensionOverStaticField() {
		return this.afterAllCalled.get() || this.beforeAllCalled.get();
	}

	@Override
	public void beforeAll(ExtensionContext extensionContext) {
		try {
			before();
		}
		finally {
			this.beforeAllCalled.set(true);
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		if (extensionOverStaticField()) {
			return;
		}
		before();
	}

	private void before() {
		stubFinder(new BatchStubRunnerFactory(builder().build(), verifier()).buildBatchStubRunner());
		stubFinder().runStubs();
	}

	private void after() {
		try {
			stubFinder().close();
		}
		catch (IOException exception) {
			LOG.warn(exception.getMessage(), exception);
		}
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) throws StubNotFoundException {
		return stubFinder().findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) throws StubNotFoundException {
		return stubFinder().findStubUrl(ivyNotation);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return stubFinder().findAllRunningStubs();
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return stubFinder().getContracts();
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		boolean result = stubFinder().trigger(ivyNotation, labelName);
		if (!result) {
			throw new IllegalStateException(
					"Failed to trigger a message with notation [" + ivyNotation + "] and label [" + labelName + "]");
		}
		return result;
	}

	@Override
	public boolean trigger(String labelName) {
		boolean result = stubFinder().trigger(labelName);
		if (!result) {
			throw new IllegalStateException("Failed to trigger a message with label [" + labelName + "]");
		}
		return result;
	}

	@Override
	public boolean trigger() {
		boolean result = stubFinder().trigger();
		if (!result) {
			throw new IllegalStateException("Failed to trigger a message");
		}
		return result;
	}

	@Override
	public Map<String, Collection<String>> labels() {
		return stubFinder().labels();
	}

	@Override
	public StubRunnerExtension messageVerifier(MessageVerifier messageVerifier) {
		verifier(messageVerifier);
		return this.delegate;
	}

	@Override
	public StubRunnerExtension options(StubRunnerOptions stubRunnerOptions) {
		builder().withOptions(stubRunnerOptions);
		return this.delegate;
	}

	@Override
	public StubRunnerExtension minPort(int minPort) {
		builder().withMinPort(minPort);
		return this.delegate;
	}

	@Override
	public StubRunnerExtension maxPort(int maxPort) {
		builder().withMaxPort(maxPort);
		return this.delegate;
	}

	@Override
	public StubRunnerExtension repoRoot(String repoRoot) {
		builder().withStubRepositoryRoot(repoRoot);
		return this.delegate;
	}

	@Override
	public StubRunnerExtension stubsMode(StubRunnerProperties.StubsMode stubsMode) {
		builder().withStubsMode(stubsMode);
		return this.delegate;
	}

	@Override
	public PortStubRunnerExtension downloadStub(String groupId, String artifactId, String version, String classifier) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + version + DELIMITER + classifier);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public PortStubRunnerExtension downloadLatestStub(String groupId, String artifactId, String classifier) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + LATEST_VERSION + DELIMITER + classifier);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public PortStubRunnerExtension downloadStub(String groupId, String artifactId, String version) {
		builder().withStubs(groupId + DELIMITER + artifactId + DELIMITER + version);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public PortStubRunnerExtension downloadStub(String groupId, String artifactId) {
		builder().withStubs(groupId + DELIMITER + artifactId);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public PortStubRunnerExtension downloadStub(String ivyNotation) {
		builder().withStubs(ivyNotation);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension downloadStubs(String... ivyNotations) {
		builder().withStubs(Arrays.asList(ivyNotations));
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension downloadStubs(List<String> ivyNotations) {
		builder().withStubs(ivyNotations);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withStubPerConsumer(boolean stubPerConsumer) {
		builder().withStubPerConsumer(stubPerConsumer);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withConsumerName(String consumerName) {
		builder().withConsumerName(consumerName);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withMappingsOutputFolder(String mappingsOutputFolder) {
		builder().withMappingsOutputFolder(mappingsOutputFolder);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withDeleteStubsAfterTest(boolean deleteStubsAfterTest) {
		builder().withDeleteStubsAfterTest(deleteStubsAfterTest);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withGenerateStubs(boolean generateStubs) {
		builder().withGenerateStubs(generateStubs);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension failOnNoStubs(boolean failOnNoStubs) {
		builder().withFailOnNoStubs(failOnNoStubs);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withProperties(Map<String, String> properties) {
		builder().withProperties(properties);
		return new PortStubRunnerExtension(this.delegate);
	}

	@Override
	public StubRunnerExtension withHttpServerStubConfigurer(
			Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer) {
		builder().withHttpServerStubConfigurer(httpServerStubConfigurer);
		return new PortStubRunnerExtension(this.delegate);
	}

	BatchStubRunner stubFinder() {
		return this.delegate.stubFinder;
	}

	void stubFinder(BatchStubRunner stubFinder) {
		this.delegate.stubFinder = stubFinder;
	}

	StubRunnerOptionsBuilder builder() {
		return this.delegate.stubRunnerOptionsBuilder;
	}

	MessageVerifier verifier() {
		return this.delegate.verifier;
	}

	void verifier(MessageVerifier verifier) {
		this.delegate.verifier = verifier;
	}

	/**
	 * Helper class with additional port, related methods once you pick a stub to
	 * download.
	 *
	 * @since 1.2.0
	 */
	public static class PortStubRunnerExtension extends StubRunnerExtension implements PortStubRunnerExtensionOptions {

		PortStubRunnerExtension(StubRunnerExtension delegate) {
			super(delegate);
		}

		@Override
		public StubRunnerExtension withPort(Integer port) {
			builder().withPort(port);
			return this.delegate;
		}

	}

}
