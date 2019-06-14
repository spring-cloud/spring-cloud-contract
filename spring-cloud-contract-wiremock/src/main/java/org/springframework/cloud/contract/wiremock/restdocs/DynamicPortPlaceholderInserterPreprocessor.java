/*
 * Copyright 2019-2019 the original author or authors.
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

package org.springframework.cloud.contract.wiremock.restdocs;

import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

class DynamicPortPlaceholderInserterPreprocessor implements OperationPreprocessor {

	private final OperationResponseFactory responseFactory = new OperationResponseFactory();

	@Override
	public OperationRequest preprocess(OperationRequest request) {
		return request;
	}

	@Override
	public OperationResponse preprocess(OperationResponse response) {
		String content = response.getContentAsString();
		if (content.contains("localhost:8080")) {
			content = content.replace("localhost:8080",
					"localhost:{{request.requestLine.port}}");
			response = this.responseFactory.createFrom(response, content.getBytes());
		}
		return response;
	}

}
