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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import wiremock.com.google.common.base.Optional;
import wiremock.org.apache.commons.io.IOUtils;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

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
		return new Request() {
			@Override
			public String getUrl() {
				return result.getRequest().getRequestURI();
			}

			@Override
			public String getAbsoluteUrl() {
				return result.getRequest().getRequestURI();
			}

			@Override
			public RequestMethod getMethod() {
				return RequestMethod.fromString(result.getRequest().getMethod());
			}

			@Override
			public String getScheme() {
				return result.getRequest().getScheme();
			}

			@Override
			public String getHost() {
				return result.getRequest().getRemoteHost();
			}

			@Override
			public int getPort() {
				return result.getRequest().getServerPort();
			}

			@Override
			public String getClientIp() {
				return "";
			}

			@Override
			public String getHeader(String key) {
				return result.getRequest().getHeader(key);
			}

			@Override
			public HttpHeader header(String key) {
				return new HttpHeader(key, getHeader(key));
			}

			@Override
			public ContentTypeHeader contentTypeHeader() {
				return new ContentTypeHeader(result.getRequest().getContentType());
			}

			@Override
			public HttpHeaders getHeaders() {
				List<HttpHeader> headers = new ArrayList<>();
				Enumeration<String> headerNames = result.getRequest().getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String key = headerNames.nextElement();
					String value = getHeader(key);
					headers.add(new HttpHeader(key, value));
				}
				return new HttpHeaders(headers);
			}

			@Override
			public boolean containsHeader(String key) {
				Enumeration<String> headerNames = result.getRequest().getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String name = headerNames.nextElement();
					if (name.equals(key)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Set<String> getAllHeaderKeys() {
				return getHeaders().keys();
			}

			@Override
			public Map<String, Cookie> getCookies() {
				Map<String, Cookie> nameToCookie = new HashMap<>();
				if (result.getRequest().getCookies() == null) {
					return nameToCookie;
				}
				for (javax.servlet.http.Cookie cookie : result.getRequest()
						.getCookies()) {
					nameToCookie.put(cookie.getName(), new Cookie(cookie.getValue()));
				}
				return nameToCookie;
			}

			@Override
			public QueryParameter queryParameter(String key) {
				return new QueryParameter(key,
						Collections.singletonList(result.getRequest().getParameter(key)));
			}

			@Override
			public byte[] getBody() {
				return result.getRequest().getContentAsByteArray();
			}

			@Override
			public String getBodyAsString() {
				try {
					return result.getRequest().getContentAsString();
				}
				catch (Exception ex) {
					return new String(result.getRequest().getContentAsByteArray());
				}
			}

			@Override
			public String getBodyAsBase64() {
				return Base64Utils
						.encodeToString(result.getRequest().getContentAsByteArray());
			}

			@Override
			public boolean isMultipart() {
				return StringUtils
						.hasText(result.getRequest().getHeader("multipart/form-data"));
			}

			@Override
			public Collection<Part> getParts() {
				try {
					return result.getRequest().getParts().stream()
							.map(part -> new Part() {
								@Override
								public String getName() {
									return part.getName();
								}

								@Override
								public HttpHeader getHeader(String name) {
									String header = part.getHeader(name);
									return new HttpHeader(name, header);
								}

								@Override
								public HttpHeaders getHeaders() {
									List<HttpHeader> headers = new ArrayList<>();
									for (String headerName : part.getHeaderNames()) {
										headers.add(new HttpHeader(headerName,
												getHeader(headerName).values()));
									}
									return new HttpHeaders(headers);
								}

								@Override
								public Body getBody() {
									try {
										return new Body(IOUtils
												.toByteArray(part.getInputStream()));
									}
									catch (IOException ex) {
										throw new IllegalStateException(ex);
									}
								}
							}).collect(Collectors.toList());
				}
				catch (Exception ex) {
					throw new IllegalStateException(ex);
				}
			}

			@Override
			public Part getPart(String name) {
				return getParts().stream().filter(part -> part.getName().equals(name))
						.findFirst().orElse(null);
			}

			@Override
			public boolean isBrowserProxyRequest() {
				return false;
			}

			@Override
			public Optional<Request> getOriginalRequest() {
				return Optional.absent();
			}
		};
	}

	@Override
	protected MediaType getContentType(MvcResult result) {
		return MediaType.valueOf(result.getRequest().getContentType());
	}

	@Override
	protected byte[] getRequestBodyContent(MvcResult result) {
		byte[] body = getWireMockRequest(result).getBody();
		return body != null ? body : new byte[0];
	}

}
