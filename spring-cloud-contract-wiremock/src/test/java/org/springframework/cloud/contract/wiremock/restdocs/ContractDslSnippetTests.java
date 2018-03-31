package org.springframework.cloud.contract.wiremock.restdocs;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
					.content("{\"foo\": 23, \"bar\" : \"baz\" }"))
				.andExpect(status().isOk())
				.andExpect(content().string("bar"))
				// first WireMock
				.andDo(WireMockRestDocs.verify()
						.jsonPath("$[?(@.foo >= 20)]")
						.jsonPath("$[?(@.bar in ['baz','bazz','bazzz'])]")
						.contentType(MediaType.valueOf("application/json"))
						.stub("shouldGrantABeerIfOldEnough"))
				// then Contract DSL documentation
				.andDo(document("index", SpringCloudContractRestDocs.dslContract()));
		//end::contract_snippet[]

		then(file("/contracts/index.groovy")).exists();
		then(file("/index/dsl-contract.adoc")).exists();
		Collection<Contract> parsedContracts = ContractVerifierDslConverter.convertAsCollection(new File("/"), file("/contracts/index.groovy"));
		Contract parsedContract = parsedContracts.iterator().next();
		then(parsedContract.getRequest().getHeaders().getEntries()).isNotNull();
		then(headerNames(parsedContract.getRequest().getHeaders().getEntries())).doesNotContain
				(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH);
		then(headerNames(parsedContract.getResponse().getHeaders().getEntries())).doesNotContain
				(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH);
		then(parsedContract.getRequest().getMethod().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue().toString()).startsWith("/");
		then(parsedContract.getRequest().getBody().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getBodyMatchers().hasMatchers()).isTrue();
		then(parsedContract.getResponse().getStatus().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getHeaders().getEntries()).isNotEmpty();
		then(parsedContract.getResponse().getBody().getClientValue()).isNotNull();
	}

	@Test
	public void should_create_contract_template_and_doc_with_placeholder_names() throws Exception {
		this.mockMvc.perform(post("/foo")
					.accept(MediaType.APPLICATION_PDF)
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"foo\": 23, \"bar\" : \"baz\" }"))
				.andExpect(status().isOk())
				.andExpect(content().string("bar"))
				// first WireMock
				.andDo(WireMockRestDocs.verify()
						.jsonPath("$[?(@.foo >= 20)]")
						.jsonPath("$[?(@.bar in ['baz','bazz','bazzz'])]")
						.contentType(MediaType.valueOf("application/json"))
						.stub("shouldGrantABeerIfOldEnough"))
				// then Contract DSL documentation
				.andDo(document("{methodName}", SpringCloudContractRestDocs.dslContract()));

		then(file("/contracts/should_create_contract_template_and_doc_with_placeholder_names.groovy")).exists();
		then(file("/should_create_contract_template_and_doc_with_placeholder_names/dsl-contract.adoc")).exists();
		Collection<Contract> parsedContracts = ContractVerifierDslConverter.convertAsCollection(new File("/"), file("/contracts/should_create_contract_template_and_doc_with_placeholder_names.groovy"));
		Contract parsedContract = parsedContracts.iterator().next();
		then(parsedContract.getRequest().getHeaders().getEntries()).isNotNull();
		then(headerNames(parsedContract.getRequest().getHeaders().getEntries())).doesNotContain
				(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH);
		then(headerNames(parsedContract.getResponse().getHeaders().getEntries())).doesNotContain
				(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH);
		then(parsedContract.getRequest().getMethod().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue().toString()).startsWith("/");
		then(parsedContract.getRequest().getBody().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getBodyMatchers().hasMatchers()).isTrue();
		then(parsedContract.getResponse().getStatus().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getHeaders().getEntries()).isNotEmpty();
		then(parsedContract.getResponse().getBody().getClientValue()).isNotNull();
	}

	@Test
	public void should_create_contract_template_and_doc_without_body_and_headers() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/foo"))
				.andExpect(status().isOk())
				.andDo(document("empty", dslContract()));

		then(file("/contracts/empty.groovy")).exists();
		then(file("/empty/dsl-contract.adoc")).exists();
		Collection<Contract> parsedContracts = ContractVerifierDslConverter.convertAsCollection(new File("/"), file("/contracts/empty.groovy"));
		Contract parsedContract = parsedContracts.iterator().next();
		then(parsedContract.getRequest().getHeaders()).isNull();
		then(parsedContract.getRequest().getMethod().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue()).isNotNull();
		then(parsedContract.getRequest().getUrl().getClientValue().toString()).startsWith("/");
		then(parsedContract.getRequest().getBody()).isNull();
		then(parsedContract.getResponse().getStatus().getClientValue()).isNotNull();
		then(parsedContract.getResponse().getHeaders()).isNull();
		then(parsedContract.getResponse().getBody()).isNull();
		then(parsedContract.getResponse().getBodyMatchers()).isNull();
	}

	private Set<String> headerNames(Set<Header> headers) {
		Set<String> names = new HashSet<>();
		for (Header header : headers) {
			names.add(header.getName());
		}
		return names;
	}

	private File file(String name) throws URISyntaxException {
		return new File(OUTPUT, name);
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