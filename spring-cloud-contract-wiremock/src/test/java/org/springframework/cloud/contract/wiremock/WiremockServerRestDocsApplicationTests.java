package org.springframework.cloud.contract.wiremock;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.assertj.core.api.BDDAssertions;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
				.andExpect(content().string("Hello World"))
				.andDo(document("resource"));
	}

	@Test
	public void statusIsMaintained() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/status"))
				.andExpect(content().string("Hello World"))
				.andExpect(status().is(HttpStatus.ACCEPTED_202))
				.andDo(document("status"));
	}

	@Test
	public void queryParamsAreFetchedFromStubs() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders
								.get("/project_metadata/spring-framework?callback=a_function_name&foo=foo&bar=bar"))
				.andExpect(status().isOk())
				.andExpect(content().string("spring-framework a_function_name foo bar"))
				.andDo(document("query"));

		File file = new File("target/snippets/stubs", "query.json");
		BDDAssertions.then(file).exists();
		StubMapping stubMapping = StubMapping.buildFrom(new String(Files.readAllBytes(file.toPath())));
		Map<String, MultiValuePattern> queryParameters = stubMapping.getRequest()
				.getQueryParameters();
		BDDAssertions.then(queryParameters.get("callback").getValuePattern())
				.isEqualTo(WireMock.equalTo("a_function_name"));
		BDDAssertions.then(queryParameters.get("foo").getValuePattern())
				.isEqualTo(WireMock.equalTo("foo"));
		BDDAssertions.then(queryParameters.get("bar").getValuePattern())
				.isEqualTo(WireMock.equalTo("bar"));
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

		@ResponseBody
		@RequestMapping("/project_metadata/{projectId}")
		public ResponseEntity<String> query(@PathVariable("projectId") String projectId,
				@RequestParam("callback") String callback,
				@RequestParam("foo") String foo,
				@RequestParam("bar") String bar) {
			return ResponseEntity.ok().body(projectId + " " + callback + " " + foo + " " + bar);
		}

	}

}
