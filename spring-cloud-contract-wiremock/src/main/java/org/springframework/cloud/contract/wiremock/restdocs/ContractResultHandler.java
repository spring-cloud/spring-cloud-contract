/*
 * Copyright 2012-2015 the original author or authors.
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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletMultipartAdapter;
import wiremock.com.google.common.base.Function;
import wiremock.com.google.common.base.Optional;
import wiremock.com.google.common.collect.ImmutableList;
import wiremock.com.google.common.collect.ImmutableMultimap;
import wiremock.com.google.common.collect.Maps;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static java.util.Collections.list;
import static wiremock.com.google.common.base.Charsets.UTF_8;
import static wiremock.com.google.common.base.MoreObjects.firstNonNull;
import static wiremock.com.google.common.base.Strings.isNullOrEmpty;
import static wiremock.com.google.common.collect.FluentIterable.from;
import static wiremock.com.google.common.collect.Lists.newArrayList;
import static wiremock.com.google.common.io.ByteStreams.toByteArray;

/**
 * Converts a {@link MvcResult} to a WireMock response.
 *
 * @author Dave Syer
 */
public class ContractResultHandler extends
		WireMockVerifyHelper<MvcResult, ContractResultHandler> implements ResultHandler {

	static final String ATTRIBUTE_NAME_CONFIGURATION = "org.springframework.restdocs.configuration";

	@Override
	public void handle(MvcResult result) throws Exception {
		configure(result);
	}

	@Override
	protected ResponseDefinitionBuilder getResponseDefinition(MvcResult result) {
		MockHttpServletResponse response = result.getResponse();
		ResponseDefinitionBuilder definition;
		try {
			definition = ResponseDefinitionBuilder.responseDefinition()
					.withBody(response.getContentAsString())
					.withStatus(response.getStatus());
			addResponseHeaders(definition, response);
			return definition;
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Cannot create response body", e);
		}
	}

	private void addResponseHeaders(ResponseDefinitionBuilder definition,
			MockHttpServletResponse input) {
		for (String name : input.getHeaderNames()) {
			definition.withHeader(name, input.getHeader(name));
		}
	}

	@Override
	protected Map<String, Object> getConfiguration(MvcResult result) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) result.getRequest()
				.getAttribute(ATTRIBUTE_NAME_CONFIGURATION);
		if (map == null) {
			map = new HashMap<>();
			result.getRequest().setAttribute(ATTRIBUTE_NAME_CONFIGURATION, map);
		}
		return map;
	}

	@Override
	protected Request getWireMockRequest(MvcResult result) {
		return new WireMockHttpServletRequestAdapter(result.getRequest());
	}

	@Override
	protected MediaType getContentType(MvcResult result) {
		return MediaType.valueOf(result.getRequest().getContentType());
	}

	@Override
	protected byte[] getRequestBodyContent(MvcResult result) {
		byte[] body = new WireMockHttpServletRequestAdapter(result.getRequest())
				.getBody();
		return body != null ? body : new byte[0];
	}

}

// COPIED FROM WIREMOCK
class WireMockHttpServletRequestAdapter implements Request {

	private static final String ORIGINAL_REQUEST_KEY = "wiremock.ORIGINAL_REQUEST";

	private final HttpServletRequest request;

	private byte[] cachedBody;

	private Collection<Part> cachedMultiparts;

	WireMockHttpServletRequestAdapter(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getUrl() {
		String url = this.request.getRequestURI();
		String contextPath = this.request.getContextPath();
		if (!isNullOrEmpty(contextPath) && url.startsWith(contextPath)) {
			url = url.substring(contextPath.length());
		}
		return withQueryStringIfPresent(url);
	}

	@Override
	public String getAbsoluteUrl() {
		return withQueryStringIfPresent(this.request.getRequestURL().toString());
	}

	private String withQueryStringIfPresent(String url) {
		return url + (isNullOrEmpty(this.request.getQueryString()) ? ""
				: "?" + this.request.getQueryString());
	}

	@Override
	public RequestMethod getMethod() {
		return RequestMethod.fromString(this.request.getMethod().toUpperCase());
	}

	@Override
	public String getScheme() {
		return this.request.getScheme();
	}

	@Override
	public String getHost() {
		return this.request.getServerName();
	}

	@Override
	public int getPort() {
		return this.request.getServerPort();
	}

	@Override
	public String getClientIp() {
		String forwardedForHeader = this.getHeader("X-Forwarded-For");

		if (forwardedForHeader != null && forwardedForHeader.length() > 0) {
			return forwardedForHeader;
		}

		return this.request.getRemoteAddr();
	}

	// Something's wrong with reading the body from request
	@Override
	public byte[] getBody() {
		if (this.cachedBody == null || this.cachedBody.length == 0) {
			try {
				if (this.request instanceof MockHttpServletRequest) {
					this.cachedBody = ((MockHttpServletRequest) this.request)
							.getContentAsByteArray();
					return this.cachedBody;
				}
				byte[] body = toByteArray(this.request.getInputStream());
				boolean isGzipped = hasGzipEncoding() || Gzip.isGzipped(body);
				this.cachedBody = isGzipped ? Gzip.unGzip(body) : body;
			}
			catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return this.cachedBody;
	}

	private Charset encodingFromContentTypeHeaderOrUtf8() {
		ContentTypeHeader contentTypeHeader = contentTypeHeader();
		if (contentTypeHeader != null) {
			return contentTypeHeader.charset();
		}
		return UTF_8;
	}

	private boolean hasGzipEncoding() {
		String encodingHeader = this.request.getHeader("Content-Encoding");
		return encodingHeader != null && encodingHeader.contains("gzip");
	}

	@Override
	public String getBodyAsString() {
		return stringFromBytes(getBody(), encodingFromContentTypeHeaderOrUtf8());
	}

	@Override
	public String getBodyAsBase64() {
		return encodeBase64(getBody());
	}

	@Override
	public String getHeader(String key) {
		List<String> headerNames = list(this.request.getHeaderNames());
		for (String currentKey : headerNames) {
			if (currentKey.toLowerCase().equals(key.toLowerCase())) {
				return this.request.getHeader(currentKey);
			}
		}
		return null;
	}

	@Override
	public HttpHeader header(String key) {
		List<String> headerNames = list(this.request.getHeaderNames());
		for (String currentKey : headerNames) {
			if (currentKey.toLowerCase().equals(key.toLowerCase())) {
				List<String> valueList = list(this.request.getHeaders(currentKey));
				if (valueList.isEmpty()) {
					return HttpHeader.empty(key);
				}
				return new HttpHeader(key, valueList);
			}
		}

		return HttpHeader.absent(key);
	}

	@Override
	public ContentTypeHeader contentTypeHeader() {
		return getHeaders().getContentTypeHeader();
	}

	@Override
	public boolean containsHeader(String key) {
		return header(key).isPresent();
	}

	@Override
	public HttpHeaders getHeaders() {
		List<HttpHeader> headerList = newArrayList();
		for (String key : getAllHeaderKeys()) {
			headerList.add(header(key));
		}

		return new HttpHeaders(headerList);
	}

	@Override
	public Set<String> getAllHeaderKeys() {
		LinkedHashSet<String> headerKeys = new LinkedHashSet<>();
		for (Enumeration<String> headerNames = this.request.getHeaderNames(); headerNames
				.hasMoreElements();) {
			headerKeys.add(headerNames.nextElement());
		}

		return headerKeys;
	}

	@Override
	public Map<String, Cookie> getCookies() {
		ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
		javax.servlet.http.Cookie[] cookies = firstNonNull(this.request.getCookies(),
				new javax.servlet.http.Cookie[0]);
		for (javax.servlet.http.Cookie cookie : cookies) {
			builder.put(cookie.getName(), cookie.getValue());
		}
		return Maps.transformValues(builder.build().asMap(),
				input -> new Cookie(null, ImmutableList.copyOf(input)));
	}

	@Override
	public QueryParameter queryParameter(String key) {
		return firstNonNull((splitQuery(this.request.getQueryString()).get(key)),
				QueryParameter.absent(key));
	}

	@Override
	public boolean isBrowserProxyRequest() {
		if (!isJetty()) {
			return false;
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<Part> getParts() {
		if (!isMultipart()) {
			return null;
		}
		if (this.cachedMultiparts == null) {
			try {
				this.cachedMultiparts = from(safelyGetRequestParts()).transform(
						(Function<javax.servlet.http.Part, Part>) WireMockHttpServletMultipartAdapter::from)
						.toList();
			}
			catch (IOException | ServletException exception) {
				return throwUnchecked(exception, Collection.class);
			}
		}
		return (this.cachedMultiparts.size() > 0) ? this.cachedMultiparts : null;
	}

	private Collection<javax.servlet.http.Part> safelyGetRequestParts()
			throws IOException, ServletException {
		try {
			return this.request.getParts();
		}
		catch (IOException ioe) {
			if (ioe.getMessage().contains("Missing content for multipart")) {
				return Collections.emptyList();
			}

			throw ioe;
		}
	}

	@Override
	public boolean isMultipart() {
		String header = getHeader("Content-Type");
		return (header != null && header.contains("multipart"));
	}

	@Override
	public Part getPart(final String name) {
		if (name == null || name.length() == 0) {
			return null;
		}
		if (this.cachedMultiparts == null) {
			if (getParts() == null) {
				return null;
			}
		}
		return from(this.cachedMultiparts)
				.firstMatch(input -> name.equals(input.getName())).get();
	}

	@Override
	public Optional<Request> getOriginalRequest() {
		Request originalRequest = (Request) this.request
				.getAttribute(ORIGINAL_REQUEST_KEY);
		return Optional.fromNullable(originalRequest);
	}

	private boolean isJetty() {
		try {
			getClass("org.eclipse.jetty.server.Request");
			return true;
		}
		catch (Exception e) {
		}
		return false;
	}

	private void getClass(String type) throws ClassNotFoundException {
		ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		ClassLoader loader = contextCL == null
				? com.github.tomakehurst.wiremock.servlet.WireMockHttpServletRequestAdapter.class
						.getClassLoader()
				: contextCL;
		Class.forName(type, false, loader);
	}

	@Override
	public String toString() {
		return this.request.toString() + getBodyAsString();
	}

}
