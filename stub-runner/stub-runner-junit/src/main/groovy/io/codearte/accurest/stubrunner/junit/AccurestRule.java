package io.codearte.accurest.stubrunner.junit;

import io.codearte.accurest.dsl.GroovyDsl;
import io.codearte.accurest.stubrunner.BatchStubRunner;
import io.codearte.accurest.stubrunner.BatchStubRunnerFactory;
import io.codearte.accurest.stubrunner.RunningStubs;
import io.codearte.accurest.stubrunner.StubConfiguration;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.StubRunnerOptions;
import io.codearte.accurest.stubrunner.StubRunnerOptionsBuilder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JUnit class rule that allows you to download the provided stubs.
 *
 * @author Marcin Grzejszczak
 */
public class AccurestRule implements TestRule, StubFinder {
	private static final String DELIMITER = ":";
	private static final String LATEST_VERSION = "+";

	private StubRunnerOptionsBuilder stubRunnerOptionsBuilder = new StubRunnerOptionsBuilder(defaultStubRunnerOptions());
	private BatchStubRunner stubFinder;

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				base.evaluate();
				stubFinder.close();
			}

			private void before() {
				stubFinder = new BatchStubRunnerFactory(stubRunnerOptionsBuilder.build()).buildBatchStubRunner();
				stubFinder.runStubs();
			}
		};
	}

	private StubRunnerOptions defaultStubRunnerOptions() {
		return new StubRunnerOptionsBuilder()
				.withMinPort(Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000")))
				.withMaxPort(Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000")))
				.withStubRepositoryRoot(System.getProperty("stubrunner.stubs.repository.root", ""))
				.withWorkOffline(Boolean.parseBoolean(System.getProperty("stubrunner.work-offline", "false")))
				.withStubsClassifier(System.getProperty("stubrunner.stubs.classifier", "stubs"))
				.withStubs(System.getProperty("stubrunner.stubs.ids", ""))
				.build();
	}

	/**
	 * Override all options
	 *
	 * @see StubRunnerOptions
	 */
	public AccurestRule options(StubRunnerOptions stubRunnerOptions) {
		stubRunnerOptionsBuilder.withOptions(stubRunnerOptions);
		return this;
	}

	/**
	 * Min value of port for WireMock server
	 */
	public AccurestRule minPort(int minPort) {
		stubRunnerOptionsBuilder.withMinPort(minPort);
		return this;
	}

	/**
	 * Max value of port for WireMock server
	 */
	public AccurestRule maxPort(int maxPort) {
		stubRunnerOptionsBuilder.withMaxPort(maxPort);
		return this;
	}

	/**
	 * String URI of repository containing stubs
	 */
	public AccurestRule repoRoot(String repoRoot) {
		stubRunnerOptionsBuilder.withStubRepositoryRoot(repoRoot);
		return this;
	}

	/**
	 * Should download stubs or use only the local repository
	 */
	public AccurestRule workOffline(boolean workOffline) {
		stubRunnerOptionsBuilder.withWorkOffline(workOffline);
		return this;
	}

	/**
	 * Group Id, artifact Id, version and classifier of a single stub to download
	 */
	public AccurestRule downloadStub(String groupId, String artifactId, String version, String classifier) {
		stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + version + DELIMITER + classifier);
		return this;
	}

	/**
	 * Group Id, artifact Id and classifier of a single stub to download in the latest version
	 */
	public AccurestRule downloadLatestStub(String groupId, String artifactId, String classifier) {
		stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + LATEST_VERSION + DELIMITER + classifier);
		return this;
	}

	/**
	 * Group Id, artifact Id and version of a single stub to download
	 */
	public AccurestRule downloadStub(String groupId, String artifactId, String version) {
		stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId + DELIMITER + version);
		return this;
	}

	/**
	 * Group Id, artifact Id of a single stub to download. Default classifier will be picked.
	 */
	public AccurestRule downloadStub(String groupId, String artifactId) {
		stubRunnerOptionsBuilder.withStubs(groupId + DELIMITER + artifactId);
		return this;
	}

	/**
	 * Ivy notation of a single stub to download.
	 */
	public AccurestRule downloadStub(String ivyNotation) {
		stubRunnerOptionsBuilder.withStubs(ivyNotation);
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public AccurestRule downloadStubs(String... ivyNotations) {
		stubRunnerOptionsBuilder.withStubs(Arrays.asList(ivyNotations));
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public AccurestRule downloadStubs(List<String> ivyNotations) {
		stubRunnerOptionsBuilder.withStubs(ivyNotations);
		return this;
	}

	/**
	 * Appends port to last added stub
	 */
	public AccurestRule withPort(Integer port) {
		stubRunnerOptionsBuilder.withPort(port);
		return this;
	}

	@Override
	public URL findStubUrl(String groupId, String artifactId) {
		return stubFinder.findStubUrl(groupId, artifactId);
	}

	@Override
	public URL findStubUrl(String ivyNotation) {
		return stubFinder.findStubUrl(ivyNotation);
	}

	@Override
	public RunningStubs findAllRunningStubs() {
		return stubFinder.findAllRunningStubs();
	}

	@Override
	public Map<StubConfiguration, Collection<GroovyDsl>> getAccurestContracts() {
		return stubFinder.getAccurestContracts();
	}

	@Override
	public boolean trigger(String ivyNotation, String labelName) {
		return stubFinder.trigger(ivyNotation, labelName);
	}

	@Override
	public boolean trigger(String labelName) {
		return stubFinder.trigger(labelName);
	}

	@Override
	public boolean trigger() {
		return stubFinder.trigger();
	}

	@Override
	public Map<String, Collection<String>> labels() {
		return stubFinder.labels();
	}

}
