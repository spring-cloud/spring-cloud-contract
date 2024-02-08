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

package org.springframework.cloud.contract.verifier.dsl.wiremock


import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.converter.YamlContractConverter
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata
import org.springframework.cloud.contract.verifier.util.ContentType
import spock.lang.Issue
import spock.lang.Specification

class WireMockRequestStubStrategySpec extends Specification {

	@Issue("#1666")
	def "should match header regex from request YAML"() {
		given:
			String yaml = """
name: upload-file
request:
  method: POST
  url: /user/upload-file
  headers:
    Content-Type: multipart/form-data;boundary=AaB03x
  multipart:
    params:
      name: "fileName.md"
    named:
      - paramName: "file"
        fileName: "fileName.md"
        fileContent: "file content"
  matchers:
    headers:
      - key: Content-Type
        regex: "multipart/form\\\\-data.*"
    multipart:
      params:
        - key: name
          regex: ".+"
      named:
        - paramName: "file"
          fileName:
            predefined: non_empty
          fileContent:
            predefined: non_empty
response:
  status: 200
  body:
    foo: bar
"""
			File tmp = File.createTempFile("foo" + System.currentTimeMillis(), ".yml")
			tmp.write(yaml)
			Contract contract = new YamlContractConverter().convertFrom(tmp).first()
		when:
			SingleContractMetadata metadata = Stub()
			metadata.evaluatedOutputStubContentType >> ContentType.JSON
			def subject = new WireMockRequestStubStrategy(contract, metadata)
			def content = subject.buildClientRequestContent()
		then:
			content.getHeaders().get("Content-Type").getValuePattern().getValue() != "multipart/form-data;boundary=AaB03x"
	}
}
