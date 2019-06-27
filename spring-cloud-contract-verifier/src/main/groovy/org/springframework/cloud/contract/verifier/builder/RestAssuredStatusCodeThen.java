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

import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class RestAssuredStatusCodeThen implements Then {

	private final BlockBuilder blockBuilder;

	private final ComparisonBuilder comparisonBuilder;

	RestAssuredStatusCodeThen(BlockBuilder blockBuilder,
			ComparisonBuilder comparisonBuilder) {
		this.blockBuilder = blockBuilder;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		Response response = metadata.getContract().getResponse();
		this.blockBuilder
				.addIndented(this.comparisonBuilder.assertThat("response.statusCode()",
						response.getStatus().getServerValue()))
				.addEndingIfNotPresent();
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}
