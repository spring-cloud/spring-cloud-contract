/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.cloud.contract.wiremock.restdocs;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentationConfigurer;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import wiremock.com.google.common.base.Optional;
import wiremock.org.apache.commons.codec.binary.Base64;

/**
 * @author Dave Syer
 *
 */
public class ContractExchangeHandler extends
		WireMockVerifyHelper<EntityExchangeResult<?>, ContractExchangeHandler>
		implements Consumer<EntityExchangeResult<byte[]>> {

	@Override
	public void accept(EntityExchangeResult<byte[]> result) {
		configure(result);
		WebTestClientRestDocumentation.document(getName()).accept(result);
	}

	@Override
	protected ResponseDefinitionBuilder getResponseDefinition(
			EntityExchangeResult<?> result) {
		ResponseDefinitionBuilder definition = ResponseDefinitionBuilder
				.responseDefinition().withBody(result.getResponseBodyContent())
				.withStatus(result.getStatus().value());
		addResponseHeaders(definition, result.getResponseHeaders());
		return definition;
	}

	private void addResponseHeaders(ResponseDefinitionBuilder definition,
			HttpHeaders httpHeaders) {
		for (String name : httpHeaders.keySet()) {
			definition.withHeader(name, httpHeaders.get(name).toArray(new String[0]));
		}
	}

	@Override
	protected Map<String, Object> getConfiguration(EntityExchangeResult<?> result) {
		Field field = ReflectionUtils.findField(
				WebTestClientRestDocumentationConfigurer.class, "configurations");
		ReflectionUtils.makeAccessible(field);
		String index = result.getRequestHeaders()
				.getFirst(WebTestClient.WEBTESTCLIENT_REQUEST_ID);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (((Map<String, Map<String, Object>>) ReflectionUtils
				.getField(field, null)).get(index));
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

	private EntityExchangeResult<?> result;

	public WireMockHttpRequestAdapter(EntityExchangeResult<?> result) {
		this.result = result;
	}

	@Override
	public String getUrl() {
		return result.getUrl().getRawPath();
	}

	@Override
	public String getAbsoluteUrl() {
		return result.getUrl().toString();
	}

	@Override
	public RequestMethod getMethod() {
		return new RequestMethod(result.getMethod().name());
	}

	@Override
	public String getClientIp() {
		return "127.0.0.1";
	}

	@Override
	public String getHeader(String key) {
		HttpHeaders headers = result.getRequestHeaders();
		return headers.containsKey(key) ? headers.getFirst(key) : null;
	}

	@Override
	public HttpHeader header(String key) {
		HttpHeaders headers = result.getRequestHeaders();
		return headers.containsKey(key)
				? new HttpHeader(key, headers.getValuesAsList(key))
				: null;
	}

	@Override
	public ContentTypeHeader contentTypeHeader() {
		MediaType contentType = result.getRequestHeaders().getContentType();
		if (contentType == null) {
			return null;
		}
		return new ContentTypeHeader(contentType.toString());
	}

	@Override
	public com.github.tomakehurst.wiremock.http.HttpHeaders getHeaders() {
		com.github.tomakehurst.wiremock.http.HttpHeaders target = new com.github.tomakehurst.wiremock.http.HttpHeaders();
		HttpHeaders headers = result.getRequestHeaders();
		for (String key : headers.keySet()) {
			target = target.plus(new HttpHeader(key, headers.getValuesAsList(key)));
		}
		return target;
	}

	@Override
	public boolean containsHeader(String key) {
		return result.getRequestHeaders().containsKey(key);
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		return result.getRequestHeaders().keySet();
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return new LinkedHashMap<>();
	}

	@Override
	public QueryParameter queryParameter(String key) {
		String query = result.getUrl().getRawQuery();
		if (query == null) {
			return null;
		}
		List<String> values = new ArrayList<>();
		for (String name : StringUtils.split(query, "&")) {
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

	@Override
	public byte[] getBody() {
		return result.getRequestBodyContent();
	}

	@Override
	public String getBodyAsString() {
		return new String(result.getRequestBodyContent(), Charset.forName("UTF-8"));
	}

	@Override
	public String getBodyAsBase64() {
		return Base64.encodeBase64String(result.getRequestBodyContent());
	}

	@Override
	public boolean isBrowserProxyRequest() {
		return false;
	}

	@Override
	public Optional<Request> getOriginalRequest() {
		return Optional.absent();
	}

}
