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

class MessagingGiven implements Given, MethodVisitor<Given>, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> givens = new LinkedList<>();

	MessagingGiven(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.givens.addAll(Arrays.asList(
				new MessagingBodyGiven(this.blockBuilder,
						new BodyReader(this.generatedClassMetaData), bodyParser),
				new MessagingHeadersGiven(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		startBodyBlock(this.blockBuilder, "given:");
		this.blockBuilder.addIndented(
				"ContractVerifierMessage inputMessage = contractVerifierMessaging.create(")
				.addEmptyLine().indent();
		this.givens.stream().filter(given -> given.accept(metadata)).forEach(given -> {
			given.apply(metadata);
			this.blockBuilder.addEmptyLine();
		});
		this.blockBuilder.unindent().unindent().startBlock().addIndented(")")
				.addEndingIfNotPresent().addEmptyLine().endBlock();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.isMessaging()
				&& metadata.getContract().getInput().getTriggeredBy() == null;
	}

}
