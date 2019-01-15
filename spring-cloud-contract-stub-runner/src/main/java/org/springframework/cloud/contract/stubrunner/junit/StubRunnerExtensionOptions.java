/*
 *  Copyright 2018 the original author or authors.
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
	 * anything a {@link ExceptionThrowingMessageVerifier} will be used.
	 * That means that an exception will be thrown whenever you try to do sth messaging
	 * related.
	 */
	StubRunnerExtension messageVerifier(MessageVerifier messageVerifier);

	/**
	 * Override all options
	 *
	 * @see StubRunnerOptions
	 */
	StubRunnerExtension options(StubRunnerOptions stubRunnerOptions);

	/**
	 * Min value of port for WireMock server
	 */
	StubRunnerExtension minPort(int minPort);

	/**
	 * Max value of port for WireMock server
	 */
	StubRunnerExtension maxPort(int maxPort);

	/**
	 * String URI of repository containing stubs
	 */
	StubRunnerExtension repoRoot(String repoRoot);

	/**
	 * Stubs mode that should be used
	 */
	StubRunnerExtension stubsMode(StubRunnerProperties.StubsMode stubsMode);

	/**
	 * Group Id, artifact Id, version and classifier of a single stub to download
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId,
			String version, String classifier);

	/**
	 * Group Id, artifact Id and classifier of a single stub to download in the latest
	 * version
	 */
	PortStubRunnerExtensionOptions downloadLatestStub(String groupId, String artifactId,
			String classifier);

	/**
	 * Group Id, artifact Id and version of a single stub to download
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId,
			String version);

	/**
	 * Group Id, artifact Id of a single stub to download. Default classifier will be
	 * picked.
	 */
	PortStubRunnerExtensionOptions downloadStub(String groupId, String artifactId);

	/**
	 * Ivy notation of a single stub to download.
	 */
	PortStubRunnerExtensionOptions downloadStub(String ivyNotation);

	/**
	 * Stubs to download in Ivy notations
	 */
	StubRunnerExtension downloadStubs(String... ivyNotations);

	/**
	 * Stubs to download in Ivy notations
	 */
	StubRunnerExtension downloadStubs(List<String> ivyNotations);

	/**
	 * Allows stub per consumer
	 */
	StubRunnerExtension withStubPerConsumer(boolean stubPerConsumer);

	/**
	 * Allows setting consumer name
	 */
	StubRunnerExtension withConsumerName(String consumerName);

	/**
	 * Allows setting the output folder for mappings
	 */
	StubRunnerExtension withMappingsOutputFolder(String mappingsOutputFolder);

	/**
	 * If set to {@code false} will NOT delete stubs from a temporary folder after running
	 * tests
	 */
	StubRunnerExtension withDeleteStubsAfterTest(boolean deleteStubsAfterTest);

	/**
	 * Map of properties that can be passed to custom
	 * {@link org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder}
	 */
	StubRunnerExtension withProperties(Map<String, String> properties);

	/**
	 * Configuration for an HTTP server stub
	 */
	StubRunnerExtension withHttpServerStubConfigurer(Class<? extends HttpServerStubConfigurer> httpServerStubConfigurer);

}
