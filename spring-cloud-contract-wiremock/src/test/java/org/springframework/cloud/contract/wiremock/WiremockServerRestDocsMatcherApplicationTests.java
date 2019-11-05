/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.wiremock;

import java.io.File;

import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
public class WiremockServerRestDocsMatcherApplicationTests {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void matchesRequest() throws Exception {
		FileSystemUtils.deleteRecursively(new File("target/snippets/stubs/posted.json"));
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/resource").content("greeting")
						.contentType(MediaType.TEXT_PLAIN))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andDo(WireMockRestDocs.verify()
						.wiremock(WireMock.post(WireMock.urlPathEqualTo("/resource"))
								.withRequestBody(WireMock.matching("greeting.*"))))
				.andDo(document("posted"));
		assertThat(new File("target/snippets/stubs/posted.json")).exists();
	}

	@Test
	public void doesNotMatch() throws Exception {
		this.expected.expect(AssertionError.class);
		this.expected.expectMessage("wiremock did not match");
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/resource").content("greeting")
						.contentType(MediaType.TEXT_PLAIN))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andDo(WireMockRestDocs.verify()
						.wiremock(WireMock.post(WireMock.urlPathEqualTo("/resource"))
								.withRequestBody(WireMock.matching("garbage.*"))))
				.andDo(document("posted"));
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
