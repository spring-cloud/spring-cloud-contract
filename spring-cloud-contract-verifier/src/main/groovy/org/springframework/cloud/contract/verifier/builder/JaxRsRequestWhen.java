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

import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;
import org.springframework.util.StringUtils;

class JaxRsRequestWhen implements When, JaxRsAcceptor, QueryParamsResolver {

	private final BlockBuilder blockBuilder;

	private final GeneratedClassMetaData generatedClassMetaData;

	JaxRsRequestWhen(BlockBuilder blockBuilder, GeneratedClassMetaData metaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		appendRequestWithRequiredResponseContentType(metadata.getContract().getRequest());
		return this;
	}

	void appendRequestWithRequiredResponseContentType(Request request) {
		String acceptHeader = getHeader(request, "Accept");
		if (StringUtils.hasText(acceptHeader)) {
			this.blockBuilder.addIndented(".request(\"" + acceptHeader + "\")");
		}
		else {
			this.blockBuilder.addIndented(".request()");
		}
	}

	private String getHeader(Request request, String name) {
		if (request.getHeaders() == null || request.getHeaders().getEntries() == null) {
			return "";
		}
		Header foundHeader = request.getHeaders().getEntries().stream()
				.filter(header -> name.equals(header.getName())).findFirst().orElse(null);
		if (foundHeader == null) {
			return "";
		}
		return MapConverter.getTestSideValuesForNonBody(foundHeader.getServerValue())
				.toString();
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}
