package io.codearte.accurest.stubrunner.junit;

import io.codearte.accurest.dsl.GroovyDsl;
import io.codearte.accurest.stubrunner.BatchStubRunner;
import io.codearte.accurest.stubrunner.BatchStubRunnerFactory;
import io.codearte.accurest.stubrunner.RunningStubs;
import io.codearte.accurest.stubrunner.StubConfiguration;
import io.codearte.accurest.stubrunner.StubFinder;
import io.codearte.accurest.stubrunner.StubRunnerOptions;
import io.codearte.accurest.stubrunner.util.StringUtils;
import io.codearte.accurest.stubrunner.util.StubsParser;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JUnit class rule that allows you to download the provided stubs.
 *
 * @author Marcin Grzejszczak
 */
public class AccurestRule implements TestRule, StubFinder {
	private static final String DELIMITER = ":";

	private Set<String> stubs = new HashSet<String>();
	private StubRunnerOptions stubRunnerOptions = defaultStubRunnerOptions();
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
				Collection<StubConfiguration> dependencies = StubsParser.fromString(stubs, stubRunnerOptions.getStubsClassifier());
				stubFinder = new BatchStubRunnerFactory(stubRunnerOptions, dependencies)
						.buildBatchStubRunner();
				stubFinder.runStubs();
			}
		};
	}

	private StubRunnerOptions defaultStubRunnerOptions() {
		Integer minPort = Integer.valueOf(System.getProperty("stubrunner.port.range.min", "10000"));
		Integer maxPort = Integer.valueOf(System.getProperty("stubrunner.port.range.max", "15000"));
		String repoRoot = System.getProperty("stubrunner.stubs.repository.root", "");
		String stubSuffix = System.getProperty("stubrunner.stubs.classifier", "stubs");
		Boolean workOffline = Boolean.parseBoolean(System.getProperty("stubrunner.work-offline", "false"));
		String stubsToDownload = System.getProperty("stubrunner.stubs.ids", "");
		if (StringUtils.hasText(stubsToDownload)) {
			Collections.addAll(stubs, stubsToDownload.split(","));
		}
		return new StubRunnerOptions(minPort, maxPort, repoRoot, workOffline, stubSuffix);
	}

	/**
	 * Override all options
	 *
	 * @see StubRunnerOptions
	 */
	public AccurestRule options(StubRunnerOptions stubRunnerOptions) {
		this.stubRunnerOptions = stubRunnerOptions;
		return this;
	}

	/**
	 * Min value of port for WireMock server
	 */
	public AccurestRule minPort(int minPort) {
		this.stubRunnerOptions.setMinPortValue(minPort);
		return this;
	}

	/**
	 * Max value of port for WireMock server
	 */
	public AccurestRule maxPort(int maxPort) {
		this.stubRunnerOptions.setMaxPortValue(maxPort);
		return this;
	}

	/**
	 * String URI of repository containing stubs
	 */
	public AccurestRule repoRoot(String repoRoot) {
		this.stubRunnerOptions.setStubRepositoryRoot(repoRoot);
		return this;
	}

	/**
	 * Should download stubs or use only the local repository
	 */
	public AccurestRule workOffline(boolean workOffline) {
		this.stubRunnerOptions.setWorkOffline(workOffline);
		return this;
	}

	/**
	 * Group Id, artifact Id and classifier of a single stub to download
	 */
	public AccurestRule downloadStub(String groupId, String artifactId, String classifier) {
		stubs.add(groupId + DELIMITER + artifactId + DELIMITER + classifier);
		return this;
	}

	/**
	 * Group Id, artifact Id of a single stub to download. Default classifier will be picked.
	 */
	public AccurestRule downloadStub(String groupId, String artifactId) {
		stubs.add(groupId + DELIMITER + artifactId);
		return this;
	}

	/**
	 * Ivy notation of a single stub to download.
	 */
	public AccurestRule downloadStub(String ivyNotation) {
		stubs.add(ivyNotation);
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public AccurestRule downloadStubs(String... ivyNotations) {
		stubs.addAll(Arrays.asList(ivyNotations));
		return this;
	}

	/**
	 * Stubs to download in Ivy notations
	 */
	public AccurestRule downloadStubs(List<String> ivyNotations) {
		stubs.addAll(ivyNotations);
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
