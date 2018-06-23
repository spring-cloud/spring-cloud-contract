package com.example.fraud;

import java.math.BigDecimal;

import com.example.fraud.model.FraudCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
@DirtiesContext
public class XmlStubGeneratorTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void should_return_full_content() throws Exception {
		mockMvc.perform(post("/xmlfraud")
				.contentType(MediaType.APPLICATION_XML)
				.content("<XmlRequestBody><name>foo</name></XmlRequestBody>"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string("<XmlResponseBody><status>FULL</status></XmlResponseBody>"))
				.andDo(MockMvcRestDocumentation.document("{methodName}"));
	}

	@Test
	public void should_return_empty_content() throws Exception {
		mockMvc.perform(post("/xmlfraud")
				.contentType(MediaType.APPLICATION_XML)
				.content("<XmlRequestBody><name></name></XmlRequestBody>"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().string("<XmlResponseBody><status>EMPTY</status></XmlResponseBody>"))
				.andDo(MockMvcRestDocumentation.document("{methodName}"));
	}

}