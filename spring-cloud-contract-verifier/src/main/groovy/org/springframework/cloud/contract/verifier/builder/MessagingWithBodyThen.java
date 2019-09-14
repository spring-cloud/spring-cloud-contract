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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MessagingWithBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	private final List<Then> thens = new LinkedList<>();

	MessagingWithBodyThen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.comparisonBuilder = comparisonBuilder;
		this.thens.addAll(Arrays.asList(
				new MessagingSpockNoMessageThen(this.blockBuilder,
						generatedClassMetaData),
				new MessagingReceiveMessageThen(this.blockBuilder, generatedClassMetaData,
						this.comparisonBuilder),
				new MessagingHeadersThen(this.blockBuilder, generatedClassMetaData,
						this.comparisonBuilder),
				new MessagingBodyThen(this.blockBuilder, generatedClassMetaData,
						comparisonBuilder),
				new MessagingAssertThatThen(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "then:");
		bodyBlock(this.blockBuilder, this.thens, singleContractMetadata);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& singleContractMetadata.getContract().getOutputMessage() != null;
	}

}
