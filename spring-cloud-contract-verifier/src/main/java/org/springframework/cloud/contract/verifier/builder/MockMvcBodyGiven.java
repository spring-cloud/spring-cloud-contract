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

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

class MockMvcBodyGiven implements Given {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	MockMvcBodyGiven(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		processInput(this.blockBuilder, metadata);
		return this;
	}

	private void processInput(BlockBuilder bb, SingleContractMetadata metadata) {
		Object body;
		Request request = metadata.getContract().getRequest();
		Object serverValue = request.getBody().getServerValue();
		if (serverValue instanceof ExecutionProperty || serverValue instanceof FromFileProperty) {
			body = request.getBody().getServerValue();
		}
		else {
			body = this.bodyParser.requestBodyAsString(metadata);
		}
		bb.addIndented(getBodyString(metadata, body));
	}

	private String getBodyString(SingleContractMetadata metadata, Object body) {
		String value;
		if (body instanceof ExecutionProperty) {
			value = body.toString();
		}
		else if (body instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) body;
			value = fileProperty.isByte()
					? this.bodyReader.readBytesFromFileString(metadata, fileProperty, CommunicationType.REQUEST)
					: this.bodyReader.readStringFromFileString(metadata, fileProperty, CommunicationType.REQUEST);
		}
		else {
			String escaped = escapeRequestSpecialChars(metadata, body.toString());
			value = this.bodyParser.quotedEscapedLongText(escaped);
		}
		return ".body(" + value + ")";
	}

	private String escapeRequestSpecialChars(SingleContractMetadata metadata, String string) {
		if (metadata.getInputTestContentType() == ContentType.JSON) {
			return string.replaceAll("\\\\n", "\\\\\\\\n");
		}
		return string;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getBody() != null;
	}

}
