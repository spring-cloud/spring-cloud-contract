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

import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class MessagingHeadersGiven implements Given, MethodVisitor<Given> {

	private final BlockBuilder blockBuilder;

	MessagingHeadersGiven(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Input inputMessage = metadata.getContract().getInput();
		this.blockBuilder.startBlock().addIndented(", headers()").startBlock();
		inputMessage.getMessageHeaders().executeForEachHeader(header -> {
			this.blockBuilder.addEmptyLine().addIndented(getHeaderString(header));
		});
		this.blockBuilder.endBlock();
		return this;
	}

	private String getHeaderString(Header header) {
		return ".header(" + getTestSideValue(header.getName()) + ", " + getTestSideValue(header.getServerValue()) + ")";
	}

	private String getTestSideValue(Object object) {
		return '"' + MapConverter.getTestSideValues(object).toString() + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageHeaders() != null;
	}

}
