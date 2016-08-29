/*
 *  Copyright 2013-2016 the original author or authors.
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

/**
 * JUnit class rule that allows you to download the provided stubs.
 *
 * @author Marcin Grzejszczak
 */
public class StubRunnerRule implements TestRule, StubFinder {
	private static final String DELIMITER = ":";
	private static final String LATEST_VERSION = "+";

	private final StubRunnerOptionsBuilder stubRunnerOptionsBuilder = new StubRunnerOptionsBuilder(defaultStubRunnerOptions());
	private BatchStubRunner stubFinder;

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				base.evaluate();
				StubRunnerRule.this.stubFinder.close();
			}

			private void before() {
				StubRunnerRule.this.stubFinder = new BatchStubRunnerFactory(
						StubRunnerRule.this.stubRunnerOptionsBuilder.build()).buildBatchStubRunner();
				StubRunnerRule.this.stubFinder.runStubs();
			}
		};
	}

	private StubRunnerOptions defaultStubRunnerOptions() {
		return new StubRunnerOptionsBuilder()
				.withMinPort(Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000")))
				.withMaxPort(Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000")))
				.withStubRepositoryRoot(System.getProperty("stubrunner.repository.root", ""))
				.withWorkOffline(Boolean.parseBoolean(System.getProperty("stubrunner.work-offline", "false")))
				.withStubsClassifier(System.getProperty("stubrunner.classifier", "stubs"))
				.withStubs(System.getProperty("stubrunner.ids", ""))
				.build();
	}

	/**
	 * Override all options
	 *
	 * @see StubRunnerOptions
	 */
	public StubRunnerRule options(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptionsBuilder.withOptions(stubRunnerOptions);
		return this;
	}

	/**
	 * Min value of port for WireMock server
	 */
	public StubRunnerRule minPort(int minPort) {
		this.stubRunnerOptionsBuilder.withMinPort(minPort);
		return this;
	}

	/**
	 * Max value of port for WireMock server
	 */
	public StubRunnerRule maxPort(int maxPort) {
		this.stubRunnerOptionsBuilder.withMaxPort(maxPort);
		return this;
	}

	/**
	 * String URI of repository containing stubs
	 */
	public StubRunnerRule repoRoot(String repoRoot) {
		this.stubRunnerOptionsBuilder.withStubRepositoryRoot(repoRoot);
		return this;
	}

	/**
	 * Should download stubs or use only the local repository
	 */
	public StubRunnerRule workOffline(boolean workOffline) {
		this.stubRunnerOptionsBuilder.withWorkOffline(workOffline);
		return this;
	}

	/**
	 * Group Id, artifact Id, version and classifier of a single stub to download
	 */
	public StubRunnerRule downloadStub(String groupId, String artifactId, String version, String classifier) {
		this.stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + version + DELIMITER + classifier);
		return this;
	}

	/**
	 * Group Id, artifact Id and classifier of a single stub to download in the latest version
	 */
	public StubRunnerRule downloadLatestStub(String groupId, String artifactId, String classifier) {
		this.stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + LATEST_VERSION + DELIMITER + classifier);
		return this;
	}

	/**
	 * Group Id, artifact Id and version of a single stub to download
	 */
	public StubRunnerRule downloadStub(String groupId, String artifactId, String version) {
		this.stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + version);
		return this;
	}

	/**
	 * Group Id, artifact Id of a single stub to download. Default classifier will be picked.
	 */
	public StubRunnerRule downloadStub(String groupId, String artifactId) {
		this.stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId);
		return this;
	}

	/**
	 * Ivy notation of a single stub to download.
	 */
	public StubRunnerRule downloadStub(String ivyNotation) {
		this.stubRunnerOptionsBuilder.withStubs(ivyNotation);
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public StubRunnerRule downloadStubs(String... ivyNotations) {
		this.stubRunnerOptionsBuilder.withStubs(Arrays.asList(ivyNotations));
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public StubRunnerRule downloadStubs(List<String> ivyNotations) {
		this.stubRunnerOptionsBuilder.withStubs(ivyNotations);
		return this;
	}

	/**
	 * Appends port to last added stub
	 */
	public StubRunnerRule withPort(Integer port) {
		this.stubRunnerOptionsBuilder.withPort(port);
		return this;
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		return this.stubFinder.findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		return this.stubFinder.findStubUrl(ivyNotation);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return this.stubFinder.findAllRunningStubs();
	}

	@Override
	public Map<StubConfiguration, Collection<Contract>> getContracts() {
		return this.stubFinder.getContracts();
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		return this.stubFinder.trigger(ivyNotation, labelName);
	}

	@Override
	public boolean trigger(String labelName) {
		return this.stubFinder.trigger(labelName);
	}

	@Override
	public boolean trigger() {
		return this.stubFinder.trigger();
	}

	@Override
	public Map<String, Collection<String>> labels() {
		return this.stubFinder.labels();
	}

}
