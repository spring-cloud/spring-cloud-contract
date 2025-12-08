/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.contract.maven.verifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

/**
 * WireMock-backed HTTP server that serves files from a Maven-style repository root so we
 * can exercise remote contract fetching over HTTP.
 */
final class WireMockRepositoryServer implements AutoCloseable {

	private static final String TRANSFORMER_NAME = "repo-file-responder";

	private final WireMockServer server;

	private WireMockRepositoryServer(WireMockServer server) {
		this.server = server;
	}

	static WireMockRepositoryServer start(Path repositoryRoot) {
		WireMockServer server = new WireMockServer(WireMockConfiguration.options()
			.dynamicPort()
			.bindAddress("127.0.0.1")
			.extensions(new RepositoryFileResponder(repositoryRoot)));
		server.start();
		server.stubFor(
				WireMock.any(WireMock.anyUrl()).willReturn(WireMock.aResponse().withTransformers(TRANSFORMER_NAME)));
		return new WireMockRepositoryServer(server);
	}

	String baseUrl() {
		return this.server.baseUrl();
	}

	@Override
	public void close() {
		this.server.stop();
	}

	private static final class RepositoryFileResponder implements ResponseDefinitionTransformerV2 {

		private final Path repositoryRoot;

		private RepositoryFileResponder(Path repositoryRoot) {
			this.repositoryRoot = repositoryRoot;
		}

		@Override
		public ResponseDefinition transform(ServeEvent serveEvent) {
			Path requested = this.repositoryRoot
				.resolve(stripLeadingSlash(stripQuery(serveEvent.getRequest().getUrl())));
			if (!Files.exists(requested) || Files.isDirectory(requested)) {
				return WireMock.aResponse().withStatus(404).build();
			}
			try {
				byte[] content = Files.readAllBytes(requested);
				return WireMock.aResponse().withStatus(200).withBody(content).build();
			}
			catch (IOException ex) {
				return WireMock.aResponse()
					.withStatus(500)
					.withBody("Failed to read file " + requested + ": " + ex.getMessage())
					.build();
			}
		}

		@Override
		public String getName() {
			return TRANSFORMER_NAME;
		}

		@Override
		public boolean applyGlobally() {
			return true;
		}

		private String stripLeadingSlash(String path) {
			if (path.startsWith("/")) {
				return path.substring(1);
			}
			return path;
		}

		private String stripQuery(String url) {
			int queryIndex = url.indexOf('?');
			if (queryIndex == -1) {
				return url;
			}
			return url.substring(0, queryIndex);
		}

	}

}
