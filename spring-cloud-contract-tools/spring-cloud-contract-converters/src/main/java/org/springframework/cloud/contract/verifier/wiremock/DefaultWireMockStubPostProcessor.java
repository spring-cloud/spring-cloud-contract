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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Metadata;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.ChunkedDribbleDelay;
import com.github.tomakehurst.wiremock.http.DelayDistribution;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import org.springframework.cloud.contract.spec.Contract;

class DefaultWireMockStubPostProcessor implements WireMockStubPostProcessor {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public StubMapping postProcess(StubMapping stubMapping, Contract contract) {
		WireMockMetaData wireMockMetaData = WireMockMetaData
				.fromMetadata(contract.getMetadata());
		StubMapping stubMappingFromMetadata = stubMappingFromMetadata(
				wireMockMetaData.getStubMapping());
		stubMapping.setResponse(mergedResponse(stubMapping, stubMappingFromMetadata));
		if (stubMappingFromMetadata.getPostServeActions() != null) {
			setPostServeActions(stubMapping, stubMappingFromMetadata);
		}
		if (stubMappingFromMetadata.getMetadata() != null) {
			setMetadata(stubMapping, stubMappingFromMetadata);
		}
		return stubMapping;
	}

	public void setPostServeActions(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		Map<String, Parameters> postServeActions = stubMapping.getPostServeActions();
		postServeActions = postServeActions != null ? postServeActions : new HashMap<>();
		postServeActions.putAll(stubMappingFromMetadata.getPostServeActions());
		stubMapping.setPostServeActions(postServeActions);
	}

	public void setMetadata(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		Metadata metadata = stubMapping.getMetadata();
		metadata = metadata != null ? metadata : new Metadata();
		metadata.putAll(stubMappingFromMetadata.getPostServeActions());
		stubMapping.setMetadata(metadata);
	}

	public ResponseDefinition mergedResponse(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		ResponseDefinition responseDefinition = new ResponseDefinition(
				stubMapping.getResponse().getStatus(),
				stubMapping.getResponse().getStatusMessage(),
				stubMapping.getResponse().getBody(),
				stubMapping.getResponse().getJsonBody(),
				stubMapping.getResponse().getBase64Body(),
				stubMapping.getResponse().getBodyFileName(),
				stubMapping.getResponse().getHeaders(),
				stubMapping.getResponse().getAdditionalProxyRequestHeaders(),
				fixedDelayMilliseconds(stubMapping, stubMappingFromMetadata),
				delayDistribution(stubMapping, stubMappingFromMetadata),
				chunkedDribbleDelay(stubMapping, stubMappingFromMetadata),
				proxyBaseUrl(stubMapping, stubMappingFromMetadata),
				fault(stubMapping, stubMappingFromMetadata),
				transformers(stubMapping, stubMappingFromMetadata),
				transformerParameters(stubMapping, stubMappingFromMetadata),
				wasConfigured(stubMapping, stubMappingFromMetadata));
		return responseDefinition;
	}

	public Boolean wasConfigured(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().isFromConfiguredStub() != null
				? stubMappingFromMetadata.getResponse().isFromConfiguredStub()
				: stubMapping.getResponse().isFromConfiguredStub();
	}

	public Parameters transformerParameters(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getTransformerParameters() != null
				? stubMappingFromMetadata.getResponse().getTransformerParameters()
				: stubMapping.getResponse().getTransformerParameters();
	}

	public List<String> transformers(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getTransformers() != null
				? stubMappingFromMetadata.getResponse().getTransformers()
				: stubMapping.getResponse().getTransformers();
	}

	public Fault fault(StubMapping stubMapping, StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getFault() != null
				? stubMappingFromMetadata.getResponse().getFault()
				: stubMapping.getResponse().getFault();
	}

	public String proxyBaseUrl(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getProxyBaseUrl() != null
				? stubMappingFromMetadata.getResponse().getProxyBaseUrl()
				: stubMapping.getResponse().getProxyBaseUrl();
	}

	public ChunkedDribbleDelay chunkedDribbleDelay(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getChunkedDribbleDelay() != null
				? stubMappingFromMetadata.getResponse().getChunkedDribbleDelay()
				: stubMapping.getResponse().getChunkedDribbleDelay();
	}

	public DelayDistribution delayDistribution(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getDelayDistribution() != null
				? stubMappingFromMetadata.getResponse().getDelayDistribution()
				: stubMapping.getResponse().getDelayDistribution();
	}

	public Integer fixedDelayMilliseconds(StubMapping stubMapping,
			StubMapping stubMappingFromMetadata) {
		return stubMappingFromMetadata.getResponse().getFixedDelayMilliseconds() != null
				? stubMappingFromMetadata.getResponse().getFixedDelayMilliseconds()
				: stubMapping.getResponse().getFixedDelayMilliseconds();
	}

	private StubMapping stubMappingFromMetadata(Object wiremock) {
		if (wiremock instanceof String) {
			return StubMapping.buildFrom((String) wiremock);
		}
		else if (wiremock instanceof StubMapping) {
			return (StubMapping) wiremock;
		}
		else if (wiremock instanceof Map) {
			try {
				return StubMapping
						.buildFrom(this.objectMapper.writeValueAsString(wiremock));
			}
			catch (JsonProcessingException e) {
				throw new IllegalStateException(
						"Failed to build StubMapping for map [" + wiremock + "]", e);
			}
		}
		throw new UnsupportedOperationException(
				"Unsupported type for wiremock metadata extension");
	}

	@Override
	public boolean isApplicable(Contract contract) {
		boolean contains = contract.getMetadata()
				.containsKey(WireMockMetaData.METADATA_KEY);
		if (!contains) {
			return false;
		}
		Object stubMapping = WireMockMetaData.fromMetadata(contract.getMetadata())
				.getStubMapping();
		return WireMockMetaData.APPLICABLE_CLASSES.stream()
				.anyMatch(aClass -> aClass.isAssignableFrom(stubMapping.getClass()));
	}

}
