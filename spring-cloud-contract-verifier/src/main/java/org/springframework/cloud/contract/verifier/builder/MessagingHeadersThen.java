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

import java.util.regex.Pattern;

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class MessagingHeadersThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	MessagingHeadersThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		endBodyBlock(this.blockBuilder);
		startBodyBlock(this.blockBuilder, "and:");
		OutputMessage outputMessage = singleContractMetadata.getContract()
				.getOutputMessage();
		outputMessage.getHeaders().executeForEachHeader(header -> {
			processHeaderElement(header.getName(),
					header.getServerValue() instanceof NotToEscapePattern
							? header.getServerValue()
							: MapConverter.getTestSideValues(header.getServerValue()));
		});
		return this;
	}

	private void appendLineWithHeaderNotNull(String property) {
		this.blockBuilder.addLineWithEnding(this.comparisonBuilder
				.assertThatIsNotNull("response.getHeader(\"" + property + "\")"));
	}

	private void processHeaderElement(String property, Object value) {
		if (value instanceof Number) {
			processHeaderElement(property, (Number) value);
		}
		else if (value instanceof Pattern) {
			processHeaderElement(property, (Pattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			processHeaderElement(property, (ExecutionProperty) value);
		}
		else {
			processHeaderElement(property, value.toString());
		}
	}

	private void processHeaderElement(String property, String value) {
		appendLineWithHeaderNotNull(property);
		this.blockBuilder.addLineWithEnding(this.comparisonBuilder.assertThat(
				"response.getHeader(\"" + property + "\").toString()", value));
	}

	private void processHeaderElement(String property, Number value) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(this.comparisonBuilder
				.assertThat("response.getHeader(\"" + property + "\")", value));
	}

	private void processHeaderElement(String property, Pattern pattern) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(this.comparisonBuilder.assertThat(
				"response.getHeader(\"" + property + "\").toString()", pattern));
	}

	private void processHeaderElement(String property, ExecutionProperty exec) {
		appendLineWithHeaderNotNull(property);
		blockBuilder.addLineWithEnding(
				exec.insertValue("response.getHeader(\"" + property + "\").toString()"));
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging() && singleContractMetadata
				.getContract().getOutputMessage().getHeaders() != null;
	}

}
