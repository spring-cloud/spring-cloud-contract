/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder;

import java.util.Arrays;

import org.springframework.cloud.contract.verifier.config.TestMode;

class MockMvcRestAssuredGroovyStaticImports
		implements Imports, GroovyLanguageAcceptor, RestAssuredVerifier {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private static final String[] REST_ASSURED_2_IMPORTS = {
			"com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	private static final String[] REST_ASSURED_3_IMPORTS = {
			"io.restassured.module.mockmvc.RestAssuredMockMvc.*" };

	MockMvcRestAssuredGroovyStaticImports(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public Imports call() {
		Arrays.stream(
				isRestAssured2Present() ? REST_ASSURED_2_IMPORTS : REST_ASSURED_3_IMPORTS)
				.forEach(s -> this.blockBuilder.addLine("import static " + s));
		return this;
	}

	@Override
	public boolean accept() {
		return acceptLanguage(generatedClassMetaData)
				&& this.generatedClassMetaData.configProperties
						.getTestMode() == TestMode.MOCKMVC
				&& this.generatedClassMetaData.isAnyHttp();
	}

}
