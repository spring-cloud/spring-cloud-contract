/*
 * Copyright 2012-2015 the original author or authors.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.servlet.WireMockHttpServletRequestAdapter;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.util.StreamUtils;

public class ContractResultHandler extends
		WireMockVerifyHelper<MvcResult, ContractResultHandler> implements ResultHandler {

	static final String ATTRIBUTE_NAME_CONFIGURATION = "org.springframework.restdocs.configuration";

	@Override
	public void handle(MvcResult result) throws Exception {
		configure(result);
		MockMvcRestDocumentation.document(getName()).handle(result);
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
		try {
			return StreamUtils.copyToByteArray(result.getRequest().getInputStream());
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot create request body", e);
		}
	}

}
