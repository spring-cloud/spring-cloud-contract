/*
 * Copyright 2018-2019 the original author or authors.
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

import java.util.List;
import java.util.Map;

import org.springframework.cloud.contract.stubrunner.HttpServerStubConfigurer;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
interface StubRunnerExtensionOptions {

	/**
	 * Pass the {@link MessageVerifier} that this rule should use. If you don't pass
	 * anything a {@link ExceptionThrowingMessageVerifier} will be used. That means that
	 * an exception will be thrown whenever you try to do sth messaging related.
	 * @param messageVerifier message verifier implementation
	 * @return the stub runner extension
	 */
	StubRunnerExtension messageVerifier(MessageVerifier messageVerifier);

	/**
	 * Override all options.
	 * @param stubRunnerOptions options of Stub Runner
	 * @return the stub runner extension
	 * @see StubRunnerOptions
	 */
	StubRunnerExtension options(StubRunnerOptions stubRunnerOptions);

	/**
	 * @param minPort min value of port for WireMock server
	 * @return the stub runner extension
	 */
	StubRunnerExtension minPort(int minPort);

	/**
	 * @param maxPort max value of port for WireMock server
	 * @return the stub runner extension
	 */
	StubRunnerExtension maxPort(int maxPort);

	/**
	 * @param repoRoot String URI of repository containing stubs
	 * @return the stub runner extension
	 */
	StubRunnerExtension repoRoot(String repoRoot);

	/**
	 * @param stubsMode Stubs mode that should be used
	 * @return the stub runner extension
	 */
	StubRunnerExtension stubsMode(StubRunnerProperties.StubsMode stubsMode);

	/**
	 * @param groupId group id of the stub
	 * @param artifactId artifact id of the stub
	 * @param version version of the stub
	 * @param classifier classifier of the stub
	 * @return the stub runner extension with ports
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId,
			String version, String classifier);

	/**
	 * @param groupId group id of the stub
	 * @param artifactId artifact id of the stub
	 * @param classifier classifier of the stub
	 * @return the stub runner extension with ports
	 */
	PortStubRunnerExtensionOptions downloadLatestStub(String groupId, String artifactId,
			String classifier);

	/**
	 * @param groupId group id of the stub
	 * @param artifactId artifact id of the stub
	 * @param version version of the stub
	 * @return the stub runner extension with ports
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId,
			String version);

	/**
	 * @param groupId group id of the stub
	 * @param artifactId artifact id of the stub
	 * @return the stub runner extension with ports
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId);

	/**
	 * @param ivyNotation Ivy notation of a single stub to download.
	 * @return the stub runner extension with ports
	 */
	PortStubRunnerExtensionOptions downloadStub(String ivyNotation);

	/**
	 * @param ivyNotations Stubs to download in Ivy notations.
	 * @return the stub runner extension
	 */
	StubRunnerExtension downloadStubs(String... ivyNotations);

	/**
	 * @param ivyNotations Stubs to download in Ivy notations.
	 * @return the stub runner extension
	 */
	StubRunnerExtension downloadStubs(List<String> ivyNotations);

	/**
	 * @param stubPerConsumer Allows stub per consumer.
	 * @return the stub runner extension
	 */
	StubRunnerExtension withStubPerConsumer(boolean stubPerConsumer);

	/**
	 * @param consumerName given consumer name
	 * @return the stub runner extension
	 */
	StubRunnerExtension withConsumerName(String consumerName);

	/**
	 * @param mappingsOutputFolder Allows setting the output folder for mappings
	 * @return the stub runner extension
	 */
	StubRunnerExtension withMappingsOutputFolder(String mappingsOutputFolder);

	/**
	 * @param deleteStubsAfterTest If set to {@code false} will NOT delete stubs from a
	 * temporary folder after running tests
	 * @return the stub runner extension
	 */
	StubRunnerExtension withDeleteStubsAfterTest(boolean deleteStubsAfterTest);

	/**
	 * @param properties Map of properties that can be passed to custom
	 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 * @return the stub runner extension
	 */
	StubRunnerExtension withProperties(Map<String, String> properties);

	/**
	 * @param httpServerStubConfigurer Configuration for an HTTP server stub
	 * @return the stub runner extension
	 */
	StubRunnerExtension withHttpServerStubConfigurer(
			Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer);

}
