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

package org.springframework.cloud.contract.verifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;

public final class TestGeneratorApplication {

	private TestGeneratorApplication() {
	}

	public static void main(String[] args) throws JsonProcessingException {
		if (args.length != 1) {
			throw new RuntimeException("Invalid number of arguments");
		}

		ObjectMapper objectMapper = new ObjectMapper();

		ContractVerifierConfigProperties configProperties = objectMapper.readValue(args[0],
				ContractVerifierConfigProperties.class);

		TestGenerator generator = new TestGenerator(configProperties);
		generator.generate();
	}

}
