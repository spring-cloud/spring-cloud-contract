/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToXmlPattern;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.cloud.contract.wiremock.WireMockStubMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.RequestCookie;
import org.springframework.restdocs.operation.ResponseCookie;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marcin Grzejszczak
 */
public class WireMockSnippetTests {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	Operation operation;

	RestDocumentationContext context;

	private File outputFolder;

	@Before
	public void setup() throws IOException {
		this.outputFolder = this.tmp.newFolder();
		ManualRestDocumentation restDocumentation = new ManualRestDocumentation(this.outputFolder.getAbsolutePath());
		restDocumentation.beforeTest(this.getClass(), "method");
		this.context = restDocumentation.beforeOperation();
		this.operation = operation(request(), response(), this.context);
	}

	@Test
	public void should_maintain_the_response_status_when_generating_stub() throws Exception {
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getResponse().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
	}

	@Test
	public void should_use_placeholders_in_stub_file_name() throws Exception {
		this.operation = operation("{method-name}/{step}", request(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/method/1.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getResponse().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
	}

	@Test
	public void should_use_equal_to_json_pattern_for_body_when_request_content_type_is_json_when_generating_stub()
			throws Exception {
		this.operation = operation(requestPostWithJsonContentType(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0)).isInstanceOf(EqualToJsonPattern.class);
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0).getValue()).isEqualTo("{\"name\": \"12\"}");
	}

	@Test
	public void should_use_equal_to_xml_pattern_for_body_when_request_content_type_is_xml_when_generating_stub()
			throws Exception {
		this.operation = operation(requestPostWithXmlContentType(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0)).isInstanceOf(EqualToXmlPattern.class);
		assertThat(stubMapping.getRequest().getBodyPatterns().get(0).getValue()).isEqualTo("<name>foo</name>");
	}

	@Test
	public void should_handle_empty_request_body() throws IOException {
		this.operation = operation(requestPostWithEmptyBody(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getRequest().getBodyPatterns()).isNullOrEmpty();
		assertThat(stubMapping.getResponse().getStatus()).isEqualTo(HttpStatus.ACCEPTED.value());
	}

	@Test
	public void should_accept_empty_value_for_query_params() throws IOException {
		this.operation = operation(requestPostWithEmptyQueryParamValue(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
	}

	@Test
	public void should_accept_query_params() throws IOException {
		this.operation = operation(requestGetWithQueryParam(), response(), this.context);
		WireMockSnippet snippet = new WireMockSnippet();

		snippet.document(this.operation);

		File stub = new File(this.outputFolder, "stubs/foo.json");
		assertThat(stub).exists();
		StubMapping stubMapping = WireMockStubMapping.buildFrom(new String(Files.readAllBytes(stub.toPath())));
		assertThat(stubMapping.getRequest().getUrlPath()).isEqualTo("/bar");
		assertThat(stubMapping.getRequest().getQueryParameters())
				.containsOnly(Assertions.entry("myParam", MultiValuePattern.of(equalTo(("myValue")))));
	}

	private Operation operation(OperationRequest request, OperationResponse response,
			RestDocumentationContext context) {
		return operation("foo", request, response, context);
	}

	private Operation operation(String name, OperationRequest request, OperationResponse response,
			RestDocumentationContext context) {
		return new Operation() {

			Map<String, Object> map = new HashMap<>();

			@Override
			public Map<String, Object> getAttributes() {
				this.map.put(RestDocumentationContext.class.getName(), context);
				return this.map;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public OperationRequest getRequest() {
				return request;
			}

			@Override
			public OperationResponse getResponse() {
				return response;
			}
		};
	}

	private OperationResponse response() {
		return new OperationResponse() {

			@Override
			public HttpStatus getStatus() {
				return HttpStatus.ACCEPTED;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}

			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return null;
			}

			@Override
			public Collection<ResponseCookie> getCookies() {
				return Collections.emptySet();
			}

		};
	}

	private OperationRequest request() {
		return new OperationRequest() {

			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return null;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.GET;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

	private OperationRequest requestPostWithJsonContentType() {
		return new OperationRequest() {

			@Override
			public byte[] getContent() {
				String content = "{\"name\": \"12\"}";
				return content.getBytes(Charset.forName("UTF-8"));
			}

			@Override
			public String getContentAsString() {
				return "{\"name\": \"12\"}";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.POST;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

	private OperationRequest requestPostWithXmlContentType() {
		return new OperationRequest() {

			@Override
			public byte[] getContent() {
				String content = "<name>foo</name>";
				return content.getBytes(Charset.forName("UTF-8"));
			}

			@Override
			public String getContentAsString() {
				return "<name>foo</name>";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add("Content-Type", MediaType.APPLICATION_XML_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.POST;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

	private OperationRequest requestPostWithEmptyBody() {
		return new OperationRequest() {
			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return "";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.POST;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

	private OperationRequest requestPostWithEmptyQueryParamValue() {
		return new OperationRequest() {
			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return "";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.POST;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar?myParam=");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

	private OperationRequest requestGetWithQueryParam() {
		return new OperationRequest() {
			@Override
			public byte[] getContent() {
				return new byte[0];
			}

			@Override
			public String getContentAsString() {
				return "";
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				return httpHeaders;
			}

			@Override
			public HttpMethod getMethod() {
				return HttpMethod.GET;
			}

			@Override
			public Collection<OperationRequestPart> getParts() {
				return null;
			}

			@Override
			public URI getUri() {
				return URI.create("https://foo/bar?myParam=myValue");
			}

			@Override
			public Collection<RequestCookie> getCookies() {
				return Collections.emptySet();
			}
		};
	}

}
