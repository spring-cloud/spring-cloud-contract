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

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class BodyAssertionLineCreator {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final String byteArrayString;

	private final ComparisonBuilder comparisonBuilder;

	BodyAssertionLineCreator(BlockBuilder blockBuilder, GeneratedClassMetaData metaData, String byteArrayString,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(metaData);
		this.byteArrayString = byteArrayString;
		this.comparisonBuilder = comparisonBuilder;
	}

	void appendBodyAssertionLine(SingleContractMetadata metadata, String property, Object value) {
		if (value instanceof String && ((String) value).startsWith("$")) {
			String newValue = stripFirstChar((String) value).replaceAll("\\$value", "responseBody" + property);
			this.blockBuilder.addLineWithEnding(newValue);
		}
		else {
			this.blockBuilder.addLineWithEnding(getResponseBodyPropertyComparisonString(metadata, property, value));
		}
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * Object {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(SingleContractMetadata singleContractMetadata,
			String property, Object value) {
		if (value instanceof FromFileProperty) {
			return getResponseBodyPropertyComparisonString(singleContractMetadata, property, (FromFileProperty) value);
		}
		else if (value instanceof Pattern) {
			return getResponseBodyPropertyComparisonString(property, (Pattern) value);
		}
		else if (value instanceof ExecutionProperty) {
			return getResponseBodyPropertyComparisonString(property, (ExecutionProperty) value);
		}
		else if (value instanceof DslProperty) {
			return getResponseBodyPropertyComparisonString(singleContractMetadata, property,
					((DslProperty) value).getServerValue());
		}
		return getResponseBodyPropertyComparisonString(property, value.toString());
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * byte[] {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(SingleContractMetadata singleContractMetadata,
			String property, FromFileProperty value) {
		if (value.isByte()) {
			return this.comparisonBuilder.assertThat(this.byteArrayString) + this.comparisonBuilder.isEqualToUnquoted(
					this.bodyReader.readBytesFromFileString(singleContractMetadata, value, CommunicationType.RESPONSE));
		}
		return getResponseBodyPropertyComparisonString(property, value.asString());
	}

	/**
	 * Builds the code that for the given {@code property} will compare it to the given
	 * String {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property, String value) {
		return this.comparisonBuilder.assertThatUnescaped("responseBody" + property, value);
	}

	/**
	 * Builds the code that for the given {@code property} will match it to the given
	 * regular expression {@code value}
	 */
	private String getResponseBodyPropertyComparisonString(String property, Pattern value) {
		return this.comparisonBuilder.assertThat("responseBody" + property, value);
	}

	/**
	 * Builds the code that for the given {@code property} will match it to the given
	 * {@link ExecutionProperty} value
	 */
	private String getResponseBodyPropertyComparisonString(String property, ExecutionProperty value) {
		return value.insertValue("responseBody" + property);
	}

	private String stripFirstChar(String s) {
		return s.substring(1);
	}

}
