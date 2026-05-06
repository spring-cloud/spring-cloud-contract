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

package org.springframework.cloud.contract.stubrunner

import org.apache.avro.generic.GenericRecord
import spock.lang.Specification

import org.springframework.cloud.contract.verifier.messaging.avro.KafkaAvroMessageVerifierSender
import org.springframework.kafka.core.KafkaTemplate

class StubRunnerExecutorAvroSpec extends Specification {

	private KafkaTemplate<String, Object> kafkaTemplate = Mock()
	private KafkaAvroMessageVerifierSender sender = new KafkaAvroMessageVerifierSender(kafkaTemplate)

	def 'should send Avro-serialized GenericRecord to Kafka for Avro contracts (bug #2404)'() {
		given:
			def tmpContractDir = saveTmpContract("""
label: book_returned
input:
  triggeredBy: publishBookReturned()
outputMessage:
  sentTo: book.returned
  headers:
    X-Correlation-Id: abc-123-def
  body:
    isbn: "978-1234567890"
    title: "Contract Testing for Dummies"
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
""")
			StubRunnerExecutor executor = new StubRunnerExecutor(new AvailablePortScanner(18000, 18999), sender, [])
			executor.runStubs(
					new StubRunnerOptionsBuilder().build(),
					new StubRepository(tmpContractDir, [], new StubRunnerOptionsBuilder().build(), null),
					new StubConfiguration('avro', 'avro', 'avro', ''))
		when:
			executor.trigger('book_returned')
		then:
			1 * kafkaTemplate.send({
				it.topic() == "book.returned" &&
				it.value() instanceof GenericRecord &&
				it.value()["schema"] != null &&
				it.value()["isbn"] == "978-1234567890" &&
				it.value()["title"] == "Contract Testing for Dummies" &&
				header(it, "X-Correlation-Id") == "abc-123-def"
			})
		cleanup:
			executor.shutdown()
			tmpContractDir.deleteDir()
	}

	private File saveTmpContract(String contractYaml) {
		File contractDir = File.createTempDir()
		new File(contractDir, "book_returned.yml").text = contractYaml
		contractDir
	}

	private String header(it, String key) {
		new String(it.headers().lastHeader(key).value())
	}

}
