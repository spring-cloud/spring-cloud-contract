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

import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MockMvcAsyncWhen implements When, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	MockMvcAsyncWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		if (response.getAsync()) {
			this.blockBuilder.addIndented(".when().async()");
		}
		if (response.getDelay() != null) {
			this.blockBuilder.addIndented(
					".timeout(" + response.getDelay().getServerValue() + ")");
		}
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		boolean accept = acceptType(this.generatedClassMetaData, metadata);
		if (!accept) {
			return false;
		}
		Response response = metadata.getContract().getResponse();
		return response.getAsync() || response.getDelay() != null;
	}

}
