package org.springframework.cloud.contract.verifier.dsl.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Marcin Grzejszczak
 */
public class WireMockStubMapping {
	public static StubMapping buildFrom(String mappingDefinition) {
		DocumentContext context = JsonPath.parse(mappingDefinition);
		context.delete("$.id");
		context.delete("$.uuid");
		return StubMapping.buildFrom(context.jsonString());
	}
}