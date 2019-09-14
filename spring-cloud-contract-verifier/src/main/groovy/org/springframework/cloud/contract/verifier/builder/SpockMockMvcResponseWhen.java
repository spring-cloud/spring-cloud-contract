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

import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class SpockMockMvcResponseWhen implements When, MockMvcAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	SpockMockMvcResponseWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		this.blockBuilder.addIndented("def response = given().spec(request)");
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata)
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
	}

}
