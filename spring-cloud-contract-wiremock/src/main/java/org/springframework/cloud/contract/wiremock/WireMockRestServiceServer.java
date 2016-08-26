/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Convenience class for loading WireMock stubs into a {@link MockRestServiceServer}. In
 * this way using a {@link RestTemplate} can mock the responses from a server using
 * WireMock JSON DSL instead of the native Java DSL.
 * 
 * @author Dave Syer
 *
 */
public class WireMockRestServiceServer {

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private String suffix = ".json";

	private String baseUrl = "";

	private MockRestServiceServerBuilder builder;

	private List<String> locations = new ArrayList<String>();

	private WireMockRestServiceServer(RestTemplate restTemplate) {
		this.builder = MockRestServiceServer.bindTo(restTemplate);
	}

	/**
	 * Public factory method for wrapping a rest template into a MockRestServiceServer.
	 * 
	 * @param restTemplate the rest template to wrap
	 * @return a WireMockRestServiceServer
	 */
	public static WireMockRestServiceServer with(RestTemplate restTemplate) {
		return new WireMockRestServiceServer(restTemplate);
	}

	/**
	 * Flag to tell the MockRestServiceServer to ignore the order of calls when matching
	 * requests.
	 * 
	 * @param ignoreExpectOrder flag value
	 * @return this
	 */
	public WireMockRestServiceServer ignoreExpectOrder(boolean ignoreExpectOrder) {
		this.builder.ignoreExpectOrder(ignoreExpectOrder);
		return this;
	}

	/**
	 * If stub locations are given as a directory, then we search recursively in that
	 * directory for files with this suffix. Default is ".json".
	 * 
	 * @param suffix the suffix to use when creating a resource pattern
	 * @return this
	 */
	public WireMockRestServiceServer suffix(String suffix) {
		this.suffix = suffix;
		return this;
	}

	/**
	 * Add a base url to all requests. Most WireMock JSON stubs have a path, but no
	 * protocol or host in the request matcher, so this is useful when your rest template
	 * is calling to a specific host.
	 * 
	 * @param baseUrl a base url to apply
	 * @return this
	 */
	public WireMockRestServiceServer baseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	/**
	 * Add some resource locations for stubs. Each location can be a resource path (to a
	 * single JSON file), or a pattern with ant-style wildcards to load all stubs that
	 * match.
	 * 
	 * @param locations a set of resource locations
	 * @return this
	 */
	public WireMockRestServiceServer stubs(String... locations) {
		this.locations.addAll(Arrays.asList(locations));
		return this;
	}

	/**
	 * Build a MockRestServiceServer from the configured stubs. The server can later be
	 * verified (optionally), if you need to check that all expected requests were made.
	 * 
	 * @return a MockRestServiceServer
	 */
	public MockRestServiceServer build() {
		MockRestServiceServer server = this.builder.build();
		for (String location : this.locations) {
			try {
				for (Resource resource : this.resolver.getResources(pattern(location))) {
					StubMapping mapping = mapping(resource);
					ResponseActions expect = server.expect(
							requestTo(this.baseUrl + mapping.getRequest().getUrlPath()));
					requestHeaders(expect, mapping.getRequest());
					expect.andRespond(response(mapping.getResponse()));
				}
			}
			catch (IOException e) {
				throw new IllegalStateException("Cannot load resources for: " + location,
						e);
			}
		}
		return server;
	}

	private String pattern(String location) {
		if (!StringUtils.getFilename(location).contains(".") && !location.contains("*")) {
			if (!location.endsWith("/")) {
				location = location + "/";
			}
			location = location + "/**/*" + this.suffix;
		}
		return location;
	}

	private StubMapping mapping(Resource resource) throws IOException {
		return Json.read(StreamUtils.copyToString(resource.getInputStream(),
				Charset.defaultCharset()), StubMapping.class);
	}

	private DefaultResponseCreator response(ResponseDefinition response) {
		return withStatus(HttpStatus.valueOf(response.getStatus()))
				.body(response.getBody()).contentType(contentType(response))
				.headers(responseHeaders(response));
	}

	private void requestHeaders(ResponseActions expect, RequestPattern request) {
		if (request.getHeaders() != null) {
			for (final String header : request.getHeaders().keySet()) {
				final MultiValuePattern pattern = request.getHeaders().get(header);
				// TODO: match the headers
			}
		}
	}

	private HttpHeaders responseHeaders(ResponseDefinition response) {
		HttpHeaders headers = new HttpHeaders();
		if (response.getHeaders() != null) {
			for (HttpHeader header : response.getHeaders().all()) {
				if (!header.keyEquals("Content-Type")) {
					for (String value : header.values()) {
						headers.add(header.key(), value);
					}
				}
			}
		}
		return headers;
	}

	private MediaType contentType(ResponseDefinition response) {
		String value = null;
		if (response.getHeaders() != null) {
			HttpHeader header = response.getHeaders().getHeader("Content-Type");
			if (header != null) {
				value = header.firstValue();
			}
		}
		return value == null ? MediaType.TEXT_PLAIN : MediaType.valueOf(value);
	}

}
