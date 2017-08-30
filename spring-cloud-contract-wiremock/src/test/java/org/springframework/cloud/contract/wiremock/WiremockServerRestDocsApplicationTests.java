package org.springframework.cloud.contract.wiremock;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WiremockServerRestDocsApplicationTests.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import wiremock.org.eclipse.jetty.http.HttpStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureMockMvc
@DirtiesContext
public class WiremockServerRestDocsApplicationTests {

	@Autowired private MockMvc mockMvc;

	@Test
	public void contextLoads() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/resource"))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andDo(document("resource"));
	}

	@Test
	public void statusIsMaintained() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/status"))
				.andExpect(MockMvcResultMatchers.content().string("Hello World"))
				.andExpect(MockMvcResultMatchers.status().is(HttpStatus.ACCEPTED_202))
				.andDo(document("status"));
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
		@RequestMapping("/status")
		public ResponseEntity<String> status() {
			return ResponseEntity.status(HttpStatus.ACCEPTED_202).body("Hello World");
		}

	}

}
