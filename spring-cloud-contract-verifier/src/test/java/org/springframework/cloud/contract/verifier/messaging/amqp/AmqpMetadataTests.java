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

package org.springframework.cloud.contract.verifier.messaging.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import static org.assertj.core.api.BDDAssertions.then;

class AmqpMetadataTests {

	YAMLMapper mapper = new YAMLMapper();

	@DisabledOnJre(JRE.JAVA_17)
	@Test
	void should_parse_the_metadata_entry() throws JsonProcessingException {
		// @formatter:off
		String yamlEntry = "amqp:\n"
				+ "  input:\n"
				+ "      connectToBroker:\n"
				+ "        additionalOptions: \"foo1\"\n"
				+ "        declareQueueWithName: \"foo2\"\n"
				+ "      messageProperties:\n"
				+ "        replyTo: \"foo3\"\n"
				+ "  outputMessage:\n"
				+ "      connectToBroker:\n"
				+ "        additionalOptions: \"bar1\"\n"
				+ "        declareQueueWithName: \"bar2\"\n"
				+ "      messageProperties:\n"
				+ "        replyTo: \"bar3\"\n";
		// @formatter:on

		AmqpMetadata metadata = AmqpMetadata
				.fromMetadata(this.mapper.readerForMapOf(Object.class).readValue(yamlEntry));

		then(metadata.getInput().getConnectToBroker().getAdditionalOptions()).isEqualTo("foo1");
		then(metadata.getInput().getConnectToBroker().getDeclareQueueWithName()).isEqualTo("foo2");
		then(metadata.getInput().getMessageProperties().getReplyTo()).isEqualTo("foo3");
		then(metadata.getOutputMessage().getConnectToBroker().getAdditionalOptions()).isEqualTo("bar1");
		then(metadata.getOutputMessage().getConnectToBroker().getDeclareQueueWithName()).isEqualTo("bar2");
		then(metadata.getOutputMessage().getMessageProperties().getReplyTo()).isEqualTo("bar3");

	}

}
