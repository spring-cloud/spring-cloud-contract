package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs.dslContract;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ContractDslSnippetTests.Config.class)
public class ContractDslSnippetTests {

	private static final String OUTPUT = "target/generated-snippets";
	@Rule
	public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(OUTPUT);

	MockMvc mockMvc;
	@Autowired WebApplicationContext context;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation))
				.build();
	}

	@Test
	public void should_create_contract_template_and_doc() throws Exception {
		//tag::contract_snippet[]
		this.mockMvc.perform(post("/foo")
					.accept(MediaType.APPLICATION_PDF)
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"foo\": 23 }"))
				.andExpect(status().isOk())
				.andExpect(content().string("bar"))
				// first WireMock
				.andDo(WireMockRestDocs.verify()
						.jsonPath("$[?(@.foo >= 20)]")
						.contentType(MediaType.valueOf("application/json"))
						.stub("shouldGrantABeerIfOldEnough"))
				// then Contract DSL documentation
				.andDo(document("index", SpringCloudContractRestDocs.dslContract()));
		//end::contract_snippet[]

		then(file("/contracts/index.groovy")).exists();
		then(file("/index/dsl-contract.adoc")).exists();
		String contract = readFromFile(file("/contracts/index.groovy"));
		// try to parse the contract
		Contract parsedContract = ContractVerifierDslConverter.convert(contract);
		then(parsedContract.getRequest().getHeaders().getEntries()).isNotEmpty();
		then(parsedContract.getRequest().getMethod().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getBody().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getStatus().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getHeaders().getEntries()).isNotEmpty();
		then(parsedContract.getResponse().getBody().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getMatchers().hasMatchers()).isTrue();
	}

	@Test
	public void should_create_contract_template_and_doc_without_body_and_headers() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/foo"))
				.andExpect(status().isOk())
				.andDo(document("empty", dslContract()));

		then(file("/contracts/empty.groovy")).exists();
		then(file("/empty/dsl-contract.adoc")).exists();
		String contract = readFromFile(file("/contracts/empty.groovy"));
		// try to parse the contract
		Contract parsedContract = ContractVerifierDslConverter.convert(contract);
		then(parsedContract.getRequest().getHeaders().getEntries()).isNotEmpty();
		then(parsedContract.getRequest().getMethod().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getBody()).isNull();
		then(parsedContract.getResponse().getStatus().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getHeaders()).isNull();
		then(parsedContract.getResponse().getBody()).isNull();
		then(parsedContract.getResponse().getMatchers()).isNull();
	}

	private File file(String name) throws URISyntaxException {
		return new File(OUTPUT, name);
	}

	private String readFromFile(File f) throws IOException {
		byte[] encoded = Files.readAllBytes(f.toPath());
		return new String(encoded, StandardCharsets.UTF_8);
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	static class Config {

		@PostMapping("/foo")
		String foo() {
			return "bar";
		}

		@GetMapping("/foo")
		void getFoo() {
		}
	}
}