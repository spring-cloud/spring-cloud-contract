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

import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MockMvcQueryParamsWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private static final String QUERY_PARAM_METHOD = "queryParam";

	private final BlockBuilder blockBuilder;

	private final BodyParser bodyParser;

	MockMvcQueryParamsWhen(BlockBuilder blockBuilder, BodyParser bodyParser) {
		this.blockBuilder = blockBuilder;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		addQueryParameters(url);
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

	private void addQueryParameters(Url buildUrl) {
		buildUrl.getQueryParameters().getParameters().stream()
				.filter(this::allowedQueryParameter).forEach(this::addQueryParameter);
	}

	private boolean allowedQueryParameter(Object o) {
		if (o instanceof QueryParameter) {
			return allowedQueryParameter(((QueryParameter) o).getServerValue());
		}
		else if (o instanceof MatchingStrategy) {
			return !MatchingStrategy.Type.ABSENT.equals(((MatchingStrategy) o).getType());
		}
		return true;
	}

	private void addQueryParameter(QueryParameter queryParam) {
		this.blockBuilder.addLine("." + QUERY_PARAM_METHOD + "("
				+ this.bodyParser.quotedLongText(queryParam.getName()) + ","
				+ this.bodyParser.quotedLongText(resolveParamValue(queryParam)) + ")");
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		Url url = getUrl(request);
		return url.getQueryParameters() != null;
	}

}
