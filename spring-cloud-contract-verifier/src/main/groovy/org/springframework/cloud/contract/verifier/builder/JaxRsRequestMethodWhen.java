/*
 * Copyright 2013-2019 the original author or authors.
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
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.util.StringUtils;

class JaxRsRequestMethodWhen implements When, JaxRsBodyParser {

	private final BlockBuilder blockBuilder;

	private final BodyReader bodyReader;

	JaxRsRequestMethodWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.bodyReader = new BodyReader(metaData);
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendMethodAndBody(metadata);
		return this;
	}

	void appendMethodAndBody(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		ContentType type = metadata.getInputTestContentType();
		String method = request.getMethod().getServerValue().toString().toLowerCase();
		if (request.getBody() != null) {
			String contentType = type.getMimeType();
			contentType = StringUtils.hasText(contentType) ? contentType
					: getContentType(request);
			Object body = request.getBody().getServerValue();
			String value;
			if (body instanceof ExecutionProperty) {
				value = body.toString();
			}
			else if (body instanceof FromFileProperty) {
				FromFileProperty fileProperty = (FromFileProperty) body;
				value = fileProperty.isByte()
						? this.bodyReader.readBytesFromFileString(metadata, fileProperty,
								CommunicationType.REQUEST)
						: this.bodyReader.readStringFromFileString(metadata, fileProperty,
								CommunicationType.REQUEST);
			}
			else {
				value = "\"" + requestBodyAsString(metadata) + "\"";
			}
			this.blockBuilder.addIndented(".build(\"" + method.toUpperCase()
					+ "\", entity(" + value + ", \"" + contentType + "\"))");
		}
		else {
			this.blockBuilder.addIndented(".build(\"" + method.toUpperCase() + "\")");
		}
	}

	private String getContentType(Request request) {
		Header contentType = request.getHeaders().getEntries().stream()
				.filter(header -> "Content-Type".equalsIgnoreCase(header.getName()))
				.findFirst().orElse(null);
		return contentType != null ? contentType.getServerValue().toString() : "";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}
