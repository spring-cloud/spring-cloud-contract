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

class MessagingBodyThen implements Then, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final ComparisonBuilder comparisonBuilder;

	private final List<Then> thens = new LinkedList<>();

	private final BodyParser bodyParser;

	MessagingBodyThen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
		this.comparisonBuilder = comparisonBuilder;
		this.bodyParser = comparisonBuilder.bodyParser();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericTextBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericJsonBodyThen(blockBuilder, metaData, this.bodyParser,
						this.comparisonBuilder),
				new GenericXmlBodyThen(blockBuilder, this.bodyParser)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		endBodyBlock(this.blockBuilder);
		startBodyBlock(this.blockBuilder, "and:");
		this.thens.stream().filter(then -> then.accept(singleContractMetadata))
				.forEach(then -> then.apply(singleContractMetadata));
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& this.bodyParser.responseBody(singleContractMetadata) != null;
	}

}
