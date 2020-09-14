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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class CustomModeThen implements Then, BodyMethodVisitor, CustomModeAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Then> thens = new LinkedList<>();

	CustomModeThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.thens.addAll(Arrays.asList(
				new CustomModeStatusCodeThen(this.blockBuilder, comparisonBuilder),
				new CustomModeHeadersThen(this.blockBuilder, comparisonBuilder),
				new CustomModeCookiesThen(this.blockBuilder, comparisonBuilder),
				new GenericHttpBodyThen(this.blockBuilder, generatedClassMetaData,
						bodyParser, comparisonBuilder)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}
