package org.springframework.cloud.contract.wiremock;

import org.json.JSONException;
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 */
public class WireMockStubMappingTest {
	private static final String stub_2_1_7 = "{\"request\" : { \"method\" : \"GET\" }, \"response\" : { \"status\" : 200 }}";
	private static final String stub_2_5_1 = "{\"id\" : \"77514bd4-a102-4478-a3c0-0fda8b905591\", \"request\" : { \"method\" : \"GET\" }, \"response\" : { \"status\" : 200 }, \"uuid\" : \"77514bd4-a102-4478-a3c0-0fda8b905591\"}";

	@Test
	public void should_successfully_parse_a_WireMock_2_1_7_stub() throws JSONException {
		// when:
		WireMockStubMapping.buildFrom(stub_2_1_7);
	}

	@Test
	public void should_successfully_parse_a_WireMock_2_5_1_stub() throws JSONException {
		// when:
		WireMockStubMapping.buildFrom(stub_2_5_1);
	}
}