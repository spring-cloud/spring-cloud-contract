package org.springframework.cloud.contract.wiremock;

import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WiremockServerRestDocsMatcherApplicationTests.TestConfiguration;
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.tomakehurst.wiremock.client.WireMock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
@DirtiesContext
public class WiremockServerRestDocsMatcherApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void matchesRequest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/resource").content("greeting")
				.contentType(MediaType.TEXT_PLAIN))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andDo(WireMockRestDocs.verify()
						.wiremock(WireMock.post(WireMock.urlPathEqualTo("/resource"))
								.withRequestBody(WireMock.matching("greeting.*")))
						.stub("posted"));
	}

	@Test
	public void doesNotMatch() throws Exception {
		expected.expect(ComparisonFailure.class);
		expected.expectMessage("wiremock did not match");
		mockMvc.perform(MockMvcRequestBuilders.post("/resource").content("greeting")
				.contentType(MediaType.TEXT_PLAIN))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andDo(WireMockRestDocs.verify()
						.wiremock(WireMock.post(WireMock.urlPathEqualTo("/resource"))
								.withRequestBody(WireMock.matching("garbage.*")))
						.stub("posted"));
	}

	@Configuration
	@RestController
	protected static class TestConfiguration {

		@ResponseBody
		@RequestMapping(value = "/resource", method = RequestMethod.POST)
		public String resource(@RequestBody String body) {
			return "Hello World";
		}

	}

}
