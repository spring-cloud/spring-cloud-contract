/*
 * Copyright 2016-present the original author or authors.
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.apache.commons.codec.binary.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentationConfigurer;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Dave Syer
 *
 */
public class ContractExchangeHandler extends WireMockVerifyHelper<EntityExchangeResult<?>, ContractExchangeHandler>
		implements Consumer<EntityExchangeResult<byte[]>> {

	@Override
	public void accept(EntityExchangeResult<byte[]> result) {
		configure(result);
	}

	@Override
	protected ResponseDefinitionBuilder getResponseDefinition(EntityExchangeResult<?> result) {
		ResponseDefinitionBuilder definition = ResponseDefinitionBuilder.responseDefinition()
			.withBody(result.getResponseBodyContent())
			.withStatus(result.getStatus().value());
		addResponseHeaders(definition, result.getResponseHeaders());
		return definition;
	}

	private void addResponseHeaders(ResponseDefinitionBuilder definition, HttpHeaders httpHeaders) {
		for (String name : httpHeaders.headerNames()) {
			definition.withHeader(name, httpHeaders.get(name).toArray(new String[0]));
		}
	}

	@Override
	protected Map<String, Object> getConfiguration(EntityExchangeResult<?> result) {
		Field field = ReflectionUtils.findField(WebTestClientRestDocumentationConfigurer.class, "configurations");
		ReflectionUtils.makeAccessible(field);
		String index = result.getRequestHeaders().getFirst(WebTestClient.WEBTESTCLIENT_REQUEST_ID);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (((Map<String, Map<String, Object>>) ReflectionUtils.getField(field, null))
			.get(index));
		if (map == null) {
			return new HashMap<>();
		}
		return map;
	}

	@Override
	protected Request getWireMockRequest(EntityExchangeResult<?> result) {
		return new WireMockHttpRequestAdapter(result);
	}

	@Override
	protected MediaType getContentType(EntityExchangeResult<?> result) {
		return result.getRequestHeaders().getContentType();
	}

	@Override
	protected byte[] getRequestBodyContent(EntityExchangeResult<?> result) {
		return result.getRequestBodyContent();
	}

}

class WireMockHttpRequestAdapter implements Request {

	private static final boolean SERVLET_API_PRESENT = ClassUtils.isPresent("jakarta.servlet.http.Part", null);

	private final EntityExchangeResult<?> result;

	WireMockHttpRequestAdapter(EntityExchangeResult<?> result) {
		this.result = result;
	}

	@Override
	public String getUrl() {
		return this.result.getUrl().getRawPath();
	}

	@Override
	public String getAbsoluteUrl() {
		return this.result.getUrl().toString();
	}

	@Override
	public RequestMethod getMethod() {
		return new RequestMethod(this.result.getMethod().name());
	}

	@Override
	public String getScheme() {
		return this.result.getUrl().getScheme();
	}

	@Override
	public String getHost() {
		return this.result.getUrl().getHost();
	}

	@Override
	public int getPort() {
		return this.result.getUrl().getPort();
	}

	@Override
	public String getClientIp() {
		return "127.0.0.1";
	}

	@Override
	public String getHeader(String key) {
		HttpHeaders headers = this.result.getRequestHeaders();
		return headers.containsHeader(key) ? headers.getFirst(key) : null;
	}

	@Override
	public HttpHeader header(String key) {
		HttpHeaders headers = this.result.getRequestHeaders();
		return headers.containsHeader(key) ? new HttpHeader(key, headers.getValuesAsList(key)) : null;
	}

	@Override
	public ContentTypeHeader contentTypeHeader() {
		MediaType contentType = this.result.getRequestHeaders().getContentType();
		if (contentType == null) {
			return null;
		}
		return new ContentTypeHeader(contentType.toString());
	}

	@Override
	public com.github.tomakehurst.wiremock.http.HttpHeaders getHeaders() {
		com.github.tomakehurst.wiremock.http.HttpHeaders target = new com.github.tomakehurst.wiremock.http.HttpHeaders();
		HttpHeaders headers = this.result.getRequestHeaders();
		for (String key : headers.headerNames()) {
			target = target.plus(new HttpHeader(key, headers.getValuesAsList(key)));
		}
		return target;
	}

	@Override
	public boolean containsHeader(String key) {
		return this.result.getRequestHeaders().containsHeader(key);
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		return this.result.getRequestHeaders().headerNames();
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return new LinkedHashMap<>();
	}

	@Override
	public QueryParameter queryParameter(String key) {
		String query = this.result.getUrl().getRawQuery();
		if (query == null) {
			return null;
		}
		List<String> values = new ArrayList<>();
		String[] strings = StringUtils.split(query, "&");
		if (strings == null) {
			return null;
		}
		for (String name : strings) {
			if (name.equals(key)) {
				values.add("");
			}
			else if (name.startsWith(key + "=")) {
				values.add(name.substring(name.indexOf("=") + 1));
			}
		}
		if (values.isEmpty()) {
			return null;
		}
		return new QueryParameter(key, values);
	}

	// ===== Helpers for application/x-www-form-urlencoded parsing =====

	private boolean isFormUrlEncoded() {
		MediaType ct = this.result.getRequestHeaders().getContentType();
		return ct != null && MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(ct);
	}

	private Charset requestCharset() {
		MediaType ct = this.result.getRequestHeaders().getContentType();
		if (ct != null && ct.getCharset() != null) {
			return ct.getCharset();
		}
		return StandardCharsets.UTF_8;
	}

	private MultiValueMap<String, String> parseFormData() {
		if (!isFormUrlEncoded()) {
			return new LinkedMultiValueMap<>();
		}
		String body = new String(this.result.getRequestBodyContent(), requestCharset());
		return UriComponentsBuilder.newInstance()
			.query(body) // treat as "?a=1&b=2&b=3"
			.build()
			.getQueryParams();
	}

	@Override
	public FormParameter formParameter(String key) {
		MultiValueMap<String, String> params = parseFormData();
		List<String> values = params.get(key);
		if (values == null || values.isEmpty()) {
			return null;
		}
		return new FormParameter(key, values);
	}

	@Override
	public Map<String, FormParameter> formParameters() {
		MultiValueMap<String, String> params = parseFormData();
		if (params.isEmpty()) {
			return Map.of();
		}
		Map<String, FormParameter> out = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> e : params.entrySet()) {
			out.put(e.getKey(), new FormParameter(e.getKey(), e.getValue()));
		}
		return out;
	}

	@Override
	public byte[] getBody() {
		return this.result.getRequestBodyContent();
	}

	@Override
	public String getBodyAsString() {
		return new String(this.result.getRequestBodyContent(), Charset.forName("UTF-8"));
	}

	@Override
	public String getBodyAsBase64() {
		return Base64.encodeBase64String(this.result.getRequestBodyContent());
	}

	@Override
	public boolean isMultipart() {
		return MediaType.MULTIPART_FORM_DATA.isCompatibleWith(this.result.getRequestHeaders().getContentType());
	}

	@Override
	public Collection<Part> getParts() {
		try {
			if (!SERVLET_API_PRESENT) {
				return Collections.emptyList();
			}
			return getWireMockParts();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// TODO: Consider caching this
	private Collection<Part> getWireMockParts() {
		MockHttpServletRequest request = MockMvcRequestBuilders
			.request(this.result.getMethod(), this.result.getUriTemplate())
			.contentType(this.result.getRequestHeaders().getContentType())
			.content(this.result.getRequestBodyContent())
			.buildRequest(new MockServletContext());
		try {
			return new StandardMultipartHttpServletRequest(request).getParts()
				.stream()
				.map(this::partFromServletPart)
				.collect(Collectors.toList());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private Part partFromServletPart(jakarta.servlet.http.Part part) {
		return new Part() {

			@Override
			public String getName() {
				return part.getName();
			}

			@Override
			public String getFileName() {
				return part.getSubmittedFileName();
			}

			@Override
			public HttpHeader getHeader(String name) {
				return new HttpHeader(name, part.getHeader(name));
			}

			@Override
			public com.github.tomakehurst.wiremock.http.HttpHeaders getHeaders() {
				com.github.tomakehurst.wiremock.http.HttpHeaders headers = new com.github.tomakehurst.wiremock.http.HttpHeaders();
				for (String s : part.getHeaderNames()) {
					headers.plus(new HttpHeader(s, part.getHeader(s)));
				}
				return headers;
			}

			@Override
			public Body getBody() {
				try {
					byte[] targetArray = new byte[part.getInputStream().available()];
					return new Body(targetArray);
				}
				catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		};
	}

	// TODO: Consider caching this
	@Override
	public Part getPart(String name) {
		return getWireMockParts().stream().filter(part -> name.equals(part.getName())).findFirst().get();
	}

	@Override
	public boolean isBrowserProxyRequest() {
		return false;
	}

	@Override
	public Optional<Request> getOriginalRequest() {
		return Optional.empty();
	}

	@Override
	public String getProtocol() {
		return this.result.getUrl().getScheme();
	}

}
