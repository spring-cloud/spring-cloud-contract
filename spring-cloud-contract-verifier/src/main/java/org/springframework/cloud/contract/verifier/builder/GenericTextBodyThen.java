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

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

import static org.springframework.cloud.contract.verifier.util.ContentType.JSON;
import static org.springframework.cloud.contract.verifier.util.ContentType.XML;

class GenericTextBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final BodyParser bodyParser;

	private final ComparisonBuilder comparisonBuilder;

	GenericTextBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder, metaData,
				this.bodyParser.byteArrayString(), this.comparisonBuilder);
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		if (convertedResponseBody instanceof String) {
			convertedResponseBody = this.bodyParser.escapeForSimpleTextAssertion(convertedResponseBody.toString());
		}
		simpleTextResponseBodyCheck(metadata, convertedResponseBody);
		return this;
	}

	private void simpleTextResponseBodyCheck(SingleContractMetadata metadata, Object convertedResponseBody) {
		this.blockBuilder.addLineWithEnding(getSimpleResponseBodyString(this.bodyParser.responseAsString()));
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "", convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	private String getSimpleResponseBodyString(String responseString) {
		return "String responseBody = " + responseString + this.blockBuilder.getLineEnding();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return outputTestContentType != JSON && outputTestContentType != XML
				&& this.bodyParser.responseBody(metadata) != null
				&& !(this.bodyParser.responseBody(metadata).getServerValue() instanceof FromFileProperty);
	}

}
