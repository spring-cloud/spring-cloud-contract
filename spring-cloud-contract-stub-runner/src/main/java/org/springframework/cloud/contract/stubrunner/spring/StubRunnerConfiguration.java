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

package org.springframework.cloud.contract.stubrunner.spring;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.AetherStubDownloader;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.stubrunner.BatchStubRunnerFactory;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.noop.NoOpStubMessages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Configuration that initializes a {@link BatchStubRunner} that runs
 * {@link org.springframework.cloud.contract.stubrunner.StubRunner} instance for each stub
 */
@Configuration
@EnableConfigurationProperties(StubRunnerProperties.class)
@ConditionalOnMissingBean(type = "org.springframework.cloud.contract.wiremock.WiremockServerConfiguration")
public class StubRunnerConfiguration {

	@Autowired(required = false)
	private MessageVerifier<?> contractVerifierMessaging;
	@Autowired(required = false)
	private StubDownloader stubDownloader;
	@Autowired
	private StubRunnerProperties props;

	/**
	 * Bean that initializes stub runners, runs them and on shutdown closes them. Upon its
	 * instantiation JAR with stubs is downloaded and unpacked to a temporary folder and
	 * WireMock server are started for each of those stubs
	 */
	@Bean
	public BatchStubRunner batchStubRunner() throws IOException {
		StubRunnerOptions stubRunnerOptions = new StubRunnerOptionsBuilder()
				.withMinMaxPort(this.props.getMinPort(), this.props.getMaxPort())
				.withStubRepositoryRoot(
						uriStringOrEmpty(this.props.getRepositoryRoot()))
				.withWorkOffline(this.props.isWorkOffline())
				.withStubsClassifier(this.props.getClassifier())
				.withStubs(this.props.getIds())
				.build();
		BatchStubRunner batchStubRunner = new BatchStubRunnerFactory(stubRunnerOptions,
				this.stubDownloader != null ? this.stubDownloader
						: new AetherStubDownloader(stubRunnerOptions),
				this.contractVerifierMessaging != null ? this.contractVerifierMessaging
						: new NoOpStubMessages()).buildBatchStubRunner();
		// TODO: Consider running it in a separate thread
		batchStubRunner.runStubs();
		return batchStubRunner;
	}

	private String uriStringOrEmpty(Resource stubRepositoryRoot) throws IOException {
		return stubRepositoryRoot != null ? stubRepositoryRoot.getURI().toString() : "";
	}

}
