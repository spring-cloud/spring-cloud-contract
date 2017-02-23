/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.wiremock.restdocs;

import java.util.Map;

import org.springframework.restdocs.snippet.Snippet;

/**
 * Convenience class for setting up RestDocs to generate a {@link org.springframework.restdocs.snippet.Snippet}
 * with Spring Cloud Contract DSL. Example usage:
 *
 * <pre>
 * &#64;RunWith(SpringRunner.class)
 * &#64;SpringBootTest
 * &#64;AutoConfigureRestDocs(outputDir = "target/snippets")
 * &#64;AutoConfigureMockMvc
 * public class ContractRestDocsApplicationTests {
 *
 * 	&#64;Autowired
 * 	private MockMvc mockMvc;
 *
 * 	&#64;Test
 * 	public void contextLoads() throws Exception {
 *     this.mockMvc.perform(post("/foo")
 *          .accept(MediaType.APPLICATION_PDF)
 *          .accept(MediaType.APPLICATION_JSON)
 *          .contentType(MediaType.APPLICATION_JSON)
 *          .content("{\"foo\": 23 }"))
 *     .andExpect(status().isOk())
 *     .andExpect(content().string("bar"))
 *     // first WireMock
 *     .andDo(WireMockRestDocs.verify()
 *          .jsonPath("$[?(&#64;.foo >= 20)]")
 *          .contentType(MediaType.valueOf("application/json"))
 *          .stub("shouldGrantABeerIfOldEnough"))
 *     // then Contract DSL documentation
 *     .andDo(document("index", SpringCloudContractRestDocs.dslContract()));
 * 	}
 * </pre>
 * 
 * which creates a file "target/snippets/contracts/index.groovy" and a
 * standard documentation entitled `dsl-contract.adoc` containing that contract.
 * 
 * @author Marcin Grzejszczak
 * @since 1.0.4
 */
public class SpringCloudContractRestDocs {

	private SpringCloudContractRestDocs() {

	}

	/**
	 * Returns a new {@code Snippet} that will document Spring Cloud Contract DSL for the API
	 * operation.
	 *
	 * @return the snippet that will document the Spring Cloud Contract DSL
	 */
	public static Snippet dslContract() {
		return new ContractDslSnippet();
	}

	/**
	 * Returns a new {@code Snippet} that will document the Spring Cloud Contract DSL for the API
	 * operation. The given {@code attributes} will be available during snippet
	 * generation.
	 *
	 * @param attributes the attributes
	 * @return the snippet that will document the Spring Cloud Contract DSL
	 */
	public static Snippet dslContract(Map<String, Object> attributes) {
		return new ContractDslSnippet(attributes);
	}

}
