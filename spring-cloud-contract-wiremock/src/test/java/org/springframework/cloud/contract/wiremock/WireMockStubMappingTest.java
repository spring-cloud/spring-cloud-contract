package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * @author Marcin Grzejszczak
 */
public class WireMockStubMappingTest {
	private static final String stub_2_1_7 = "{\"request\" : { \"method\" : \"GET\" }, \"response\" : { \"status\" : 200 }}";
	private static final String stub_2_5_1 = "{\"id\" : \"77514bd4-a102-4478-a3c0-0fda8b905591\", \"request\" : { \"method\" : \"GET\" }, \"response\" : { \"status\" : 200 }, \"uuid\" : \"77514bd4-a102-4478-a3c0-0fda8b905591\"}";

	@Test
	public void should_successfully_parse_a_WireMock_2_1_7_stub() {
		// when:
		StubMapping mapping = WireMockStubMapping.buildFrom(stub_2_1_7);
		// then:
		JSONAssert.assertEquals(stub_2_1_7, mapping.toString(), false);
	}

	@Test
	public void should_successfully_parse_a_WireMock_2_5_1_stub() {
		// when:
		StubMapping mapping = WireMockStubMapping.buildFrom(stub_2_5_1);
		// then:
		JSONAssert.assertEquals(stub_2_1_7, mapping.toString(), false);
	}
}