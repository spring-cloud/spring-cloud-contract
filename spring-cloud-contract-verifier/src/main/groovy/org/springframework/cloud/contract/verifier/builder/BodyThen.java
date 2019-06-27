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

import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

interface BodyThen {

	default DslProperty requestBody(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getBody();
	}

	default DslProperty responseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody();
	}

	default BodyMatchers responseBodyMatchers(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBodyMatchers();
	}

}
