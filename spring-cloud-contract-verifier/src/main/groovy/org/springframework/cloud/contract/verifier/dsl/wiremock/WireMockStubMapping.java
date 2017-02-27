package org.springframework.cloud.contract.verifier.dsl.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * @author Marcin Grzejszczak
 */
public class WireMockStubMapping {
	public static StubMapping buildFrom(String mappingDefinition) {
		return StubMapping.buildFrom(mappingDefinition);
	}
}