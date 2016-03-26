package io.codearte.accurest.stubrunner.spring;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import io.codearte.accurest.stubrunner.BatchStubRunner;
import io.codearte.accurest.stubrunner.BatchStubRunnerFactory;
import io.codearte.accurest.stubrunner.StubConfiguration;
import io.codearte.accurest.stubrunner.StubRunner;
import io.codearte.accurest.stubrunner.StubRunnerOptions;
import io.codearte.accurest.stubrunner.StubRunning;
import io.codearte.accurest.stubrunner.util.StubsParser;

/**
 * Configuration that initializes a {@link BatchStubRunner} that runs {@link StubRunner} instance for each stub
 */
@Configuration
public class StubRunnerConfiguration {

	/**
	 * Bean that initializes stub runners, runs them and on shutdown closes them. Upon its instantiation
	 * JAR with stubs is downloaded and unpacked to a temporary folder and WireMock server are started
	 * for each of those stubs
	 *
	 * @param minPortValue       min port value of the WireMock instance for stubs
	 * @param maxPortValue       max port value of the WireMock instance for stubs
	 * @param stubRepositoryRoot root URL from where the JAR with stub mappings will be downloaded
	 * @param stubsSuffix        classifier for the jar containing stubs
	 * @param workOffline        forces offline work
	 * @param stubs              comma separated list of stubs presented in Ivy notation
	 */
	@Bean(initMethod = "runStubs", destroyMethod = "close")
	public StubRunning batchStubRunner(
			@Value("${stubrunner.port.range.min:10000}") Integer minPortValue,
			@Value("${stubrunner.port.range.max:15000}") Integer maxPortValue,
			@Value("${stubrunner.stubs.repository.root:}") Resource stubRepositoryRoot,
			@Value("${stubrunner.stubs.classifier:stubs}") String stubsSuffix,
			@Value("${stubrunner.work-offline:false}") boolean workOffline,
			@Value("${stubrunner.stubs:}") String stubs) throws IOException {
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptions(minPortValue,
				maxPortValue, uriStringOrEmpty(stubRepositoryRoot),
				stubRepositoryRoot == null || workOffline, stubsSuffix);
		Set<StubConfiguration> dependencies = StubsParser.fromString(stubs, stubsSuffix);
		return new BatchStubRunnerFactory(stubRunnerOptions, dependencies)
				.buildBatchStubRunner();
	}

	private String uriStringOrEmpty(Resource stubRepositoryRoot) throws IOException {
		return stubRepositoryRoot != null ? stubRepositoryRoot.getURI().toString() : "";
	}

}
