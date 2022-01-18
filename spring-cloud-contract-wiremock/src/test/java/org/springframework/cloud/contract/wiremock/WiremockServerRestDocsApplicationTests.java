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

package org.springframework.cloud.contract.wiremock;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WiremockServerRestDocsApplicationTests.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
@Disabled("jakarta")
public class WiremockServerRestDocsApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void contextLoads() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/resource")).andExpect(content().string("Hello World"))
				.andDo(document("resource"));
	}

	@Test
	public void statusIsMaintained() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/status")).andExpect(content().string("Hello World"))
				.andExpect(status().is(HttpStatus.ACCEPTED_202)).andDo(document("status"));
	}

	@Test
	public void queryParamsAreFetchedFromStubs() throws Exception {
		this.mockMvc
				.perform(MockMvcRequestBuilders
						.get("/project_metadata/spring-framework?callback=a_function_name&foo=foo&bar=bar"))
				.andExpect(status().isOk()).andExpect(content().string("spring-framework a_function_name foo bar"))
				.andDo(document("query"));

		File file = new File("target/snippets/stubs", "query.json");
		BDDAssertions.then(file).exists();
		StubMapping stubMapping = StubMapping.buildFrom(new String(Files.readAllBytes(file.toPath())));
		Map<String, MultiValuePattern> queryParameters = stubMapping.getRequest().getQueryParameters();
		BDDAssertions.then(queryParameters.get("callback").getValuePattern())
				.isEqualTo(WireMock.equalTo("a_function_name"));
		BDDAssertions.then(queryParameters.get("foo").getValuePattern()).isEqualTo(WireMock.equalTo("foo"));
		BDDAssertions.then(queryParameters.get("bar").getValuePattern()).isEqualTo(WireMock.equalTo("bar"));
	}

	@Test
	public void stubsRenderLinksWithPlaceholder() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/link")).andExpect(status().isOk())
				.andExpect(content().string(containsString("link:"))).andDo(document("link"));

		File file = new File("target/snippets/stubs", "link.json");
		BDDAssertions.then(file).exists();
		StubMapping stubMapping = StubMapping.buildFrom(new String(Files.readAllBytes(file.toPath())));
		String body = stubMapping.getResponse().getBody();
		BDDAssertions.then(body).contains("http://localhost:{{request.requestLine.port}}/link");
		BDDAssertions.then(stubMapping.getResponse().getTransformers()).contains("response-template");
	}

	@Configuration
	@RestController
	protected static class TestConfiguration {

		@ResponseBody
		@RequestMapping("/resource")
		public String resource() {
			return "Hello World";
		}

		@ResponseBody
		@RequestMapping("/link")
		public String link(HttpServletRequest request) {
			// UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest(new
			// ServletServerHttpRequest(request))
			// .build();
			// return "link: " + uriComponents.toUriString();
			return "";
		}

		@ResponseBody
		@RequestMapping("/status")
		public ResponseEntity<String> status() {
			return ResponseEntity.status(HttpStatus.ACCEPTED_202).body("Hello World");
		}

		@ResponseBody
		@RequestMapping("/project_metadata/{projectId}")
		public ResponseEntity<String> query(@PathVariable("projectId") String projectId,
				@RequestParam("callback") String callback, @RequestParam("foo") String foo,
				@RequestParam("bar") String bar) {
			return ResponseEntity.ok().body(projectId + " " + callback + " " + foo + " " + bar);
		}

	}

}
