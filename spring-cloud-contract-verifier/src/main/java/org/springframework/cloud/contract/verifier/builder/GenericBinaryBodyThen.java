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

class GenericBinaryBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyAssertionLineCreator bodyAssertionLineCreator;

	private final BodyParser bodyParser;

	private final ComparisonBuilder comparisonBuilder;

	GenericBinaryBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyAssertionLineCreator = new BodyAssertionLineCreator(blockBuilder, metaData,
				bodyParser.byteArrayString(), this.comparisonBuilder);
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Object responseBody = this.bodyParser.responseBody(metadata).getServerValue();
		byteResponseBodyCheck(metadata, (FromFileProperty) responseBody);
		return this;
	}

	private void byteResponseBodyCheck(SingleContractMetadata metadata, FromFileProperty convertedResponseBody) {
		this.bodyAssertionLineCreator.appendBodyAssertionLine(metadata, "", convertedResponseBody);
		this.blockBuilder.addEndingIfNotPresent();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Object responseBody = this.bodyParser.responseBody(metadata).getServerValue();
		if (!(responseBody instanceof FromFileProperty)) {
			return false;
		}
		return ((FromFileProperty) responseBody).isByte();
	}

}
