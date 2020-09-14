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

class CustomModeWhen implements When, BodyMethodVisitor, CustomModeAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> responseWhens = new LinkedList<>();

	CustomModeWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.responseWhens.addAll(Arrays.asList(
				new CustomModeResponseWhen(blockBuilder, this.generatedClassMetaData)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		addResponseWhenLine(singleContractMetadata);
		this.blockBuilder.addEmptyLine();
		return this;
	}

	private void addResponseWhenLine(SingleContractMetadata singleContractMetadata) {
		this.responseWhens.stream().filter(when -> when.accept(singleContractMetadata))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching request building When implementation for Rest Assured"))
				.apply(singleContractMetadata);
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}
