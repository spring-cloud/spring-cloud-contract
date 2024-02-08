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

class JaxRsWhen implements When, BodyMethodVisitor, JaxRsAcceptor {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	private final JaxRsBodyParser bodyParser;

	private final List<When> whens = new LinkedList<>();

	JaxRsWhen(BlockBuilder blockBuilder, GeneratedClassMetaData generatedClassMetaData, JaxRsBodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.bodyParser = bodyParser;
		this.whens
				.addAll(Arrays.asList(new JaxRsUrlPathWhen(this.blockBuilder, this.generatedClassMetaData, bodyParser),
						new JaxRsRequestWhen(this.blockBuilder, this.generatedClassMetaData),
						new JaxRsRequestHeadersWhen(this.blockBuilder, bodyParser),
						new JaxRsRequestCookiesWhen(this.blockBuilder, bodyParser),
						new JaxRsRequestMethodWhen(this.blockBuilder, this.generatedClassMetaData),
						new JaxRsRequestInvokerWhen(this.blockBuilder)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		startBodyBlock(this.blockBuilder, "when:");
		this.blockBuilder.addIndented("Response response = webTarget");
		this.blockBuilder.indent();
		indentedBodyBlock(this.blockBuilder, this.whens, singleContractMetadata);
		this.blockBuilder.addEmptyLine().endBlock();
		if (expectsResponseBody(singleContractMetadata)) {
			this.blockBuilder.addLineWithEnding("String responseAsString = " + this.bodyParser.readEntity());
		}
		this.blockBuilder.endBlock();
		return this;
	}

	private boolean expectsResponseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody() != null;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}
