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

/**
 * Convenience class for setting up RestDocs to record WireMock stubs. Example usage:
 * 
 * <pre>
 * &#64;RunWith(SpringRunner.class)
 * &#64;SpringBootTest
 * &#64;AutoConfigureRestDocs(outputDir = "target/snippets")
 * &#64;AutoConfigureMockMvc
 * public class WiremockServerRestDocsApplicationTests {
 * 
 * 	&#64;Autowired
 * 	private MockMvc mockMvc;
 * 
 * 	&#64;Test
 * 	public void contextLoads() throws Exception {
 * 		mockMvc.perform(get("/resource"))
 * 				.andExpect(content().string("Hello World"))
 * 				.andDo(verify().contract("resource"));
 * 	}
 * </pre>
 * 
 * which creates a file "target/snippets/stubs/resource.json" matching any GET request to
 * "/resource". To match POST and PUT, you can also specify the content type using
 * <code>verify().contentType(...)</code> and JSON content of the body using
 * <code>verify().jsonPath(...)</code>.
 * 
 * @author Dave Syer
 *
 */
public class WireMockRestDocs {

	public static ContractRequestHandler verify() {
		return new ContractRequestHandler();
	}

}
