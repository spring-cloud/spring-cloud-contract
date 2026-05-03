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


import org.springframework.cloud.contract.verifier.converter.YamlContract
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification
import tools.jackson.dataformat.yaml.YAMLMapper

class KafkaAvroMessageVerifierSenderSpec extends Specification {

	static final String DUMMY_ISBN = "978-1234567890"
	static final String DUMMY_TITLE = "Contract Testing for Dummies"

	KafkaTemplate<String, Object> kafkaTemplate = Mock()
	KafkaAvroMessageVerifierSender sender = new KafkaAvroMessageVerifierSender(kafkaTemplate)
	YAMLMapper yamlMapper = new YAMLMapper()

	def "should parse yml contract with inline schema and send avro message to kafka"() {
		given:
			def contractYaml = """
label: book_returned
input:
  triggeredBy: publishBookReturned()
outputMessage:
  sentTo: book.returned
  body:
    isbn: "$DUMMY_ISBN"
    title: "$DUMMY_TITLE"
metadata:
  kafka:
    avro:
      schema: >
        {
          "type": "record",
          "name": "Book",
          "fields": [
            {"name": "isbn", "type": "string"},
            {"name": "title", "type": "string"}
          ]
        }
"""
			YamlContract contract = yamlMapper.readerFor(YamlContract).readValue(contractYaml)
			Map<String, Object> payload = [isbn: DUMMY_ISBN, title: DUMMY_TITLE]

		when:
			sender.send(payload, [:], "book.returned", contract)

		then:
			1 * kafkaTemplate.send("book.returned", {
				it["isbn"] == DUMMY_ISBN &&
				it["title"] == DUMMY_TITLE
			})
	}

	def "should parse yml contract with classpath schema and send avro message to kafka"() {
		given:
			def contractYaml = """
label: book_returned
input:
  triggeredBy: publishBookReturned()
outputMessage:
  sentTo: book.returned
  body:
    isbn: "$DUMMY_ISBN"
    title: "$DUMMY_TITLE"
metadata:
  kafka:
    avro:
      schema: classpath:avro/Book.avsc
"""
			YamlContract contract = yamlMapper.readerFor(YamlContract).readValue(contractYaml)
			Map<String, Object> payload = [isbn: DUMMY_ISBN, title: DUMMY_TITLE]

		when:
			sender.send(payload, [:], "book.returned", contract)

		then:
			1 * kafkaTemplate.send("book.returned", {
				it["isbn"] == DUMMY_ISBN &&
				it["title"] == DUMMY_TITLE
			})
	}
}
