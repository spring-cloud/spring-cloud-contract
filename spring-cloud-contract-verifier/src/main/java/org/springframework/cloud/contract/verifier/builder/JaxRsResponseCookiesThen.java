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

package org.springframework.cloud.contract.verifier.builder;

import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class JaxRsResponseCookiesThen implements Then, MockMvcAcceptor, CookieElementProcessor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	private final BodyParser bodyParser;

	JaxRsResponseCookiesThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyParser = comparisonBuilder.bodyParser();
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		processCookies(metadata);
		return this;
	}

	@Override
	public ComparisonBuilder comparisonBuilder() {
		return this.comparisonBuilder;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public String cookieKey(String key) {
		return "response.getCookies().get(" + this.bodyParser.quotedShortText(key) + ")";
	}

	@Override
	public String cookieValue(String key) {
		return cookieKey(key) + ".getValue()";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getCookies() != null;
	}

}
