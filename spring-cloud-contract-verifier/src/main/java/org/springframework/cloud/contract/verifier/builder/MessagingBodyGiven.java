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
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

class MessagingBodyGiven implements Given, MethodVisitor<Given> {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	MessagingBodyGiven(BlockBuilder blockBuilder, BodyReader bodyReader,
			BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = bodyReader;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		appendBodyGiven(metadata);
		return this;
	}

	private void appendBodyGiven(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputTestContentType();
		Input inputMessage = metadata.getContract().getInput();
		Object bodyValue = this.bodyParser.extractServerValueFromBody(contentType,
				inputMessage.getMessageBody().getServerValue());
		if (bodyValue instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) bodyValue;
			String byteText = fileProperty.isByte()
					? this.bodyReader.readBytesFromFileString(metadata, fileProperty,
							CommunicationType.REQUEST)
					: this.bodyParser.quotedLongText(
							this.bodyReader.readStringFromFileString(metadata,
									fileProperty, CommunicationType.REQUEST));
			this.blockBuilder.addIndented(byteText);
		}
		else {
			String text = this.bodyParser.convertToJsonString(bodyValue);
			this.blockBuilder.addIndented(this.bodyParser.quotedEscapedLongText(text));
		}
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageBody() != null;
	}

}
