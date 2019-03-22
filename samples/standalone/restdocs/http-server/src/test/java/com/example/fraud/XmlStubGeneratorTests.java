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

package com.example.fraud;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
public class XmlStubGeneratorTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void should_return_full_content() throws Exception {
		mockMvc.perform(post("/xmlfraud").contentType(MediaType.APPLICATION_XML)
				.content("<XmlRequestBody><name>foo</name></XmlRequestBody>"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string(
						"<XmlResponseBody><status>FULL</status></XmlResponseBody>"))
				.andDo(MockMvcRestDocumentation.document("{methodName}"));
	}

	@Test
	public void should_return_empty_content() throws Exception {
		mockMvc.perform(post("/xmlfraud").contentType(MediaType.APPLICATION_XML)
				.content("<XmlRequestBody><name></name></XmlRequestBody>"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string(
						"<XmlResponseBody><status>EMPTY</status></XmlResponseBody>"))
				.andDo(MockMvcRestDocumentation.document("{methodName}"));
	}

}
