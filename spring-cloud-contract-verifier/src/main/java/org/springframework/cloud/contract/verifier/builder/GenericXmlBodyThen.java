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

import java.util.Optional;

import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

import static org.springframework.cloud.contract.verifier.util.ContentType.XML;

class GenericXmlBodyThen implements Then {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	GenericXmlBodyThen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		BodyMatchers bodyMatchers = this.bodyParser.responseBodyMatchers(metadata);
		Object convertedResponseBody = this.bodyParser.convertResponseBody(metadata);
		XmlBodyVerificationBuilder xmlBodyVerificationBuilder = new XmlBodyVerificationBuilder(metadata.getContract(),
				Optional.of(this.blockBuilder.getLineEnding()));
		xmlBodyVerificationBuilder.addXmlResponseBodyCheck(this.blockBuilder, convertedResponseBody, bodyMatchers,
				this.bodyParser.responseAsString(), true);
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		ContentType outputTestContentType = metadata.getOutputTestContentType();
		return XML == outputTestContentType;
	}

}
