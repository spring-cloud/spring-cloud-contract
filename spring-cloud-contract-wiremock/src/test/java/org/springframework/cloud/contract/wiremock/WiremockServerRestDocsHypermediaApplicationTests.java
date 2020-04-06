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

import javax.servlet.http.HttpServletRequest;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WiremockServerRestDocsHypermediaApplicationTests.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class,
		properties = "wiremock.placeholders.enabled=false")
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
public class WiremockServerRestDocsHypermediaApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void stubsRenderLinksWithoutPlaceholder() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/link"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("link:")))
				.andDo(document("link"));

		File file = new File("target/snippets/stubs", "link.json");
		BDDAssertions.then(file).exists();
		StubMapping stubMapping = StubMapping
				.buildFrom(new String(Files.readAllBytes(file.toPath())));
		String body = stubMapping.getResponse().getBody();
		BDDAssertions.then(body).contains("http://localhost:8080/link");
	}

	@Configuration
	@RestController
	protected static class TestConfiguration {

		@ResponseBody
		@RequestMapping("/link")
		public String resource(HttpServletRequest request) {
			UriComponents uriComponents = UriComponentsBuilder
					.fromHttpRequest(new ServletServerHttpRequest(request)).build();
			return "link: " + uriComponents.toUriString();
		}

	}

}
