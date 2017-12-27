package org.springframework.cloud.contract.stubrunner.junit;

import java.util.List;

import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

interface StubRunnerRuleOptions {
	/**
	 * Pass the {@link MessageVerifier} that this rule should use.
	 * If you don't pass anything a {@link StubRunnerRule.ExceptionThrowingMessageVerifier} will be used.
	 * That means that an exception will be thrown whenever you try to do sth messaging related.
	 */
	StubRunnerRule messageVerifier(MessageVerifier messageVerifier);

	/**
	 * Override all options
	 *
	 * @see StubRunnerOptions
	 */
	StubRunnerRule options(StubRunnerOptions stubRunnerOptions);

	/**
	 * Min value of port for WireMock server
	 */
	StubRunnerRule minPort(int minPort);

	/**
	 * Max value of port for WireMock server
	 */
	StubRunnerRule maxPort(int maxPort);

	/**
	 * String URI of repository containing stubs
	 */
	StubRunnerRule repoRoot(String repoRoot);

	/**
	 * Stubs mode that should be used
	 */
	StubRunnerRule stubsMode(StubRunnerProperties.StubsMode stubsMode);

	/**
	 * Group Id, artifact Id, version and classifier of a single stub to download
	 */
	PortStubRunnerRuleOptions downloadStub(String groupId, String artifactId, String version,
			String classifier);

	/**
	 * Group Id, artifact Id and classifier of a single stub to download in the latest version
	 */
	PortStubRunnerRuleOptions downloadLatestStub(String groupId, String artifactId,
			String classifier);

	/**
	 * Group Id, artifact Id and version of a single stub to download
	 */
	PortStubRunnerRuleOptions downloadStub(String groupId, String artifactId,
			String version);

	/**
	 * Group Id, artifact Id of a single stub to download. Default classifier will be picked.
	 */
	PortStubRunnerRuleOptions downloadStub(String groupId, String artifactId);

	/**
	 * Ivy notation of a single stub to download.
	 */
	PortStubRunnerRuleOptions downloadStub(String ivyNotation);

	/**
	 * Stubs to download in Ivy notations
	 */
	StubRunnerRule downloadStubs(String... ivyNotations);

	/**
	 * Stubs to download in Ivy notations
	 */
	StubRunnerRule downloadStubs(List<String> ivyNotations);

	/**
	 * Allows stub per consumer
	 */
	StubRunnerRule withStubPerConsumer(boolean stubPerConsumer);

	/**
	 * Allows setting consumer name
	 */
	StubRunnerRule withConsumerName(String consumerName);

	/**
	 * Allows setting the output folder for mappings
	 */
	StubRunnerRule withMappingsOutputFolder(String mappingsOutputFolder);
}
