/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

class ContractVerifierMetadataTests {

	YAMLMapper mapper = new YAMLMapper();

	@Test
	void should_parse_the_metadata_entry() throws JsonProcessingException {
		// @formatter:off
		String yamlEntry = "verifier:\n"
				+ "  tool: graphql";
		// @formatter:on

		ContractVerifierMetadata metadata = ContractVerifierMetadata
				.fromMetadata(this.mapper.readerForMapOf(Object.class).readValue(yamlEntry));

		String serialized = this.mapper.writer().forType(ContractVerifierMetadata.class).writeValueAsString(metadata);
		BDDAssertions.then(serialized)
				.isEqualToNormalizingPunctuationAndWhitespace(yamlEntry.replace("verifier:\n", ""));
	}

}
