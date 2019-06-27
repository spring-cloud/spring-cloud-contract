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

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class MockMvcUrlWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	MockMvcUrlWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		addUrl(url, request);
		return this;
	}

	private Url getUrl(Request request) {
		if (request.getUrl() != null) {
			return request.getUrl();
		}
		if (request.getUrlPath() != null) {
			return request.getUrlPath();
		}
		throw new IllegalStateException("URL is not set!");
	}

	private void addUrl(Url buildUrl, Request request) {
		Object testSideUrl = MapConverter.getTestSideValues(buildUrl);
		String method = request.getMethod().getServerValue().toString().toLowerCase();
		String url = testSideUrl.toString();
		if (!(testSideUrl instanceof ExecutionProperty)) {
			url = this.bodyParser.quotedShortText(testSideUrl.toString());
		}
		this.blockBuilder.addIndented("." + method + "(" + url + ")");
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}
