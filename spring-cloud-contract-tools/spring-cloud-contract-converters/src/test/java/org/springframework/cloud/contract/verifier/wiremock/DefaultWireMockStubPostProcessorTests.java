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

package org.springframework.cloud.contract.verifier.wiremock;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.contract.spec.Contract;

import static org.assertj.core.api.BDDAssertions.then;

class DefaultWireMockStubPostProcessorTests {

	// @formatter:off
	private static final String STUB_MAPPING = "{\n" + "    \"request\": {\n"
			+ "        \"method\": \"GET\",\n" + "        \"url\": \"/ping\"\n"
			+ "    },\n" + "    \"response\": {\n" + "        \"status\": 200,\n"
			+ "        \"body\": \"pong\",\n" + "        \"headers\": {\n"
			+ "            \"Content-Type\": \"text/plain\"\n" + "        }\n" + "    }\n"
			+ "}";

	private static final String POST_SERVE_ACTION = "{ \"postServeActions\": {\n"
			+ "      \"webhook\": {\n" + "        \"headers\": {\n"
			+ "          \"Content-Type\": \"application/json\"\n" + "        },\n"
			+ "        \"method\": \"POST\",\n"
			+ "        \"body\": \"{ \\\"result\\\": \\\"SUCCESS\\\" }\",\n"
			+ "        \"url\": \"http://localhost:56299/callback\"\n" + "      }\n"
			+ "    } }";

	private static final String RESPONSE_DELAY = "{\n"
			+ "    \"response\": {\n"
			+ "            \"delayDistribution\": {\n"
			+ "                    \"type\": \"lognormal\",\n"
			+ "                    \"median\": 80,\n"
			+ "                    \"sigma\": 0.4\n"
			+ "            }\n"
			+ "    }\n"
			+ "}\n";
	// @formatter:on

	@Test
	void should_not_be_applicable_for_missing_metadata_entry() {
		then(new DefaultWireMockStubPostProcessor().isApplicable(new Contract()))
				.isFalse();
	}

	@Test
	void should_not_be_applicable_for_invalid_metadata_entry() {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping", 5);
		contract.getMetadata().put("wiremock", map);

		then(new DefaultWireMockStubPostProcessor().isApplicable(contract)).isFalse();
	}

	@Test
	void should_be_applicable_for_valid_metadata_entry() {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping", "foo");
		contract.getMetadata().put("wiremock", map);

		then(new DefaultWireMockStubPostProcessor().isApplicable(contract)).isTrue();

		map.put("stubMapping", new StubMapping());

		then(new DefaultWireMockStubPostProcessor().isApplicable(contract)).isTrue();

		map.put("stubMapping", new HashMap<>());

		then(new DefaultWireMockStubPostProcessor().isApplicable(contract)).isTrue();
	}

	@Test
	void should_merge_stub_mappings_when_stub_mapping_is_string() {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping", POST_SERVE_ACTION);
		contract.getMetadata().put("wiremock", map);
		StubMapping stubMapping = StubMapping.buildFrom(STUB_MAPPING);

		StubMapping result = new DefaultWireMockStubPostProcessor()
				.postProcess(stubMapping, contract);

		thenPostServerActionWasSet(result);
	}

	@Test
	void should_merge_stub_mappings_when_stub_mapping_is_stub_mapping() {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping", StubMapping.buildFrom(POST_SERVE_ACTION));
		contract.getMetadata().put("wiremock", map);
		StubMapping stubMapping = StubMapping.buildFrom(STUB_MAPPING);

		StubMapping result = new DefaultWireMockStubPostProcessor()
				.postProcess(stubMapping, contract);

		thenPostServerActionWasSet(result);
	}

	@Test
	void should_merge_stub_mappings_when_stub_mapping_is_map()
			throws JsonProcessingException {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping",
				new ObjectMapper().readValue(POST_SERVE_ACTION, HashMap.class));
		contract.getMetadata().put("wiremock", map);
		StubMapping stubMapping = StubMapping.buildFrom(STUB_MAPPING);

		StubMapping result = new DefaultWireMockStubPostProcessor()
				.postProcess(stubMapping, contract);

		thenPostServerActionWasSet(result);
	}

	@Test
	void should_merge_stub_mappings_when_stub_mapping_is_string_and_contains_response() {
		Contract contract = new Contract();
		Map<String, Object> map = new HashMap<>();
		map.put("stubMapping", RESPONSE_DELAY);
		contract.getMetadata().put("wiremock", map);
		StubMapping stubMapping = StubMapping.buildFrom(STUB_MAPPING);

		StubMapping result = new DefaultWireMockStubPostProcessor()
				.postProcess(stubMapping, contract);

		then(result.getRequest().getMethod().getName()).isEqualTo("GET");
		then(result.getResponse().getStatus()).isEqualTo(200);
		then(result.getResponse().getBody()).isEqualTo("pong");
		then(result.getResponse().getHeaders().size()).isEqualTo(1);
		then(result.getResponse().getDelayDistribution()).isNotNull();
	}

	private void thenPostServerActionWasSet(StubMapping result) {
		then(result.getRequest().getMethod().getName()).isEqualTo("GET");
		then(result.getResponse().getStatus()).isEqualTo(200);
		then(result.getResponse().getBody()).isEqualTo("pong");
		then(result.getPostServeActions()).containsKey("webhook");
		Parameters webhook = result.getPostServeActions().get("webhook");
		then(webhook.getString("method")).isEqualTo("POST");
	}

}
