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

import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class RestAssuredWhen implements When, BodyMethodVisitor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> responseWhens = new LinkedList<>();

	private final List<When> whens = new LinkedList<>();

	RestAssuredWhen(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.responseWhens.addAll(Arrays.asList(
				new MockMvcResponseWhen(blockBuilder, this.generatedClassMetaData),
				new SpockMockMvcResponseWhen(blockBuilder, this.generatedClassMetaData),
				new ExplicitResponseWhen(blockBuilder, this.generatedClassMetaData),
				new WebTestClientResponseWhen(blockBuilder,
						this.generatedClassMetaData)));
		this.whens.addAll(
				Arrays.asList(new MockMvcQueryParamsWhen(this.blockBuilder, bodyParser),
						new MockMvcAsyncWhen(this.blockBuilder,
								this.generatedClassMetaData),
						new MockMvcUrlWhen(this.blockBuilder, bodyParser)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		addResponseWhenLine(singleContractMetadata);
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
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
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}
