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

import java.util.Iterator;

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class CustomModeHeadersThen implements Then, CustomModeAcceptor {

	private final BlockBuilder blockBuilder;

	private final ComparisonBuilder comparisonBuilder;

	CustomModeHeadersThen(BlockBuilder blockBuilder, ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		Headers headers = response.getHeaders();
		Iterator<Header> iterator = headers.getEntries().iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			String text = processHeaderElement(header.getName(), header.getServerValue() instanceof NotToEscapePattern
					? header.getServerValue() : MapConverter.getTestSideValues(header.getServerValue()));
			if (iterator.hasNext()) {
				this.blockBuilder.addLineWithEnding(text);
			}
			else {
				this.blockBuilder.addIndented(text);
			}
		}
		this.blockBuilder.addEndingIfNotPresent();
		return this;
	}

	private String processHeaderElement(String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			return this.comparisonBuilder.assertThat("response.header(\"" + property + "\")")
					+ matchesManuallyEscapedPattern((NotToEscapePattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			return ((ExecutionProperty) value).insertValue("response.header(\"" + property + "\")");

		}
		return this.comparisonBuilder.assertThat("response.header(\"" + property + "\")", value);
	}

	private String matchesManuallyEscapedPattern(NotToEscapePattern value) {
		return this.comparisonBuilder.matchesEscaped(value.getServerValue().pattern().replace("\\", "\\\\"));
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		return response.getHeaders() != null;
	}

}
