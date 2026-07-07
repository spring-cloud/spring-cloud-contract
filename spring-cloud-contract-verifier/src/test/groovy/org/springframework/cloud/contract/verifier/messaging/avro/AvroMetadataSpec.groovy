/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.contract.verifier.messaging.avro

import spock.lang.Specification
import tools.jackson.dataformat.yaml.YAMLMapper

import org.springframework.cloud.contract.verifier.messaging.kafka.KafkaMetadata

class AvroMetadataSpec extends Specification {

	YAMLMapper mapper = new YAMLMapper()

	def "should parse avro metadata nested under kafka"() {
		given:
			def yamlEntry = """
kafka:
  avro:
    schema: classpath:avro/Book.avsc
"""
		when:
			def parsed = mapper.readerForMapOf(Object).readValue(yamlEntry)
			KafkaMetadata kafkaMetadata = KafkaMetadata.fromMetadata(parsed)
		then:
			kafkaMetadata.avro.schema == "classpath:avro/Book.avsc"
	}

	def "should return empty avro metadata when avro key is absent"() {
		given:
			def yamlEntry = """
kafka:
  outputMessage:
    connectToBroker:
      additionalOptions: foo
"""
		when:
			def parsed = mapper.readerForMapOf(Object).readValue(yamlEntry)
			KafkaMetadata kafkaMetadata = KafkaMetadata.fromMetadata(parsed)
		then:
			kafkaMetadata.avro.schema == null
	}

}
