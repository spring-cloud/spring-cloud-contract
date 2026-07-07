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


import org.apache.kafka.clients.producer.ProducerRecord
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
		1 * kafkaTemplate.send({
			it.topic() == "book.returned" &&
					it.value()["isbn"] == DUMMY_ISBN &&
					it.value()["title"] == DUMMY_TITLE
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
		1 * kafkaTemplate.send({ ProducerRecord record ->
			record.topic() == "book.returned" &&
					record.value()["isbn"] == DUMMY_ISBN &&
					record.value()["title"] == DUMMY_TITLE
		})
	}

	def "should propagate headers to the kafka ProducerRecord"() {
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
		Map<String, Object> headers = ["X-Correlation-Id": "abc-123", "Content-Type": "avro/binary"]
		when:
		sender.send(payload, headers, "book.returned", contract)
		then:
		1 * kafkaTemplate.send({
			it.topic() == "book.returned" &&
					header(it, "X-Correlation-Id") == "abc-123" &&
					header(it, "Content-Type") == "avro/binary"
		})
	}

	def "should fail when StubRunnerExecutor passes a JSON string payload instead of a map (bug #2404)"() {
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
		String jsonPayload = """{"isbn":"$DUMMY_ISBN","title":"$DUMMY_TITLE"}"""
		when:
		sender.send(jsonPayload, [:], "book.returned", contract)
		then:
		thrown(IllegalArgumentException)
	}

	String header(ProducerRecord record, String key) {
		new String(record.headers().lastHeader(key).value())
	}
}
