package org.springframework.cloud.contract.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Marcin Grzejszczak
 */
public class WireMock2_1_7_StubMapping {
	public static StubMapping buildFrom(String mappingDefinition) {
		DocumentContext context = JsonPath.parse(mappingDefinition);
		context.delete("$.id");
		context.delete("$.uuid");
		return StubMapping.buildFrom(context.jsonString());
	}
}