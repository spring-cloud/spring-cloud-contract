/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.converter

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class YamlContractConverterSpec extends Specification {

	URL ymlUrl = YamlContractConverterSpec.getResource("/contract.yml")
	File yml = new File(ymlUrl.toURI())
	YamlContractConverter converter = new YamlContractConverter()

	def "should convert YAML to DSL"() {
		given:
			assert converter.isAccepted(yml)
		when:
			Collection<Contract> contracts = converter.convertFrom(yml)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.request.url.clientValue == "/foo"
			contract.request.method.clientValue == "PUT"
			contract.request.headers.entries.find { it.name == "foo" && it.clientValue == "bar" }
			contract.request.body.clientValue == [foo: "bar"]
			contract.response.status.clientValue == 200
			contract.response.headers.entries.find { it.name == "foo2" && it.clientValue == "bar" }
			contract.response.body.clientValue == [foo2: "bar"]
	}

	def "should convert DSL to YAML"() {
		given:
			assert converter.isAccepted(yml)
		and:
			List<Contract> contracts = [Contract.make {
				request {
					url("/foo")
					method("PUT")
					headers {
						header("foo", "bar")
					}
					body([foo: "bar"])
				}
				response {
					status(200)
					headers {
						header("foo2", "bar")
					}
					body([foo2: "bar"])
				}
			}]
		when:
			Collection<YamlContract> yamlContracts = converter.convertTo(contracts)
		then:
			yamlContracts.size() == 1
			YamlContract yamlContract = yamlContracts.first()
			yamlContract.request.url == "/foo"
			yamlContract.request.method == "PUT"
			yamlContract.request.headers.find { it.key == "foo" && it.value == "bar" }
			yamlContract.request.body == [foo: "bar"]
			yamlContract.response.status == 200
			yamlContract.response.headers.find { it.key == "foo2" && it.value == "bar" }
			yamlContract.response.body == [foo2: "bar"]
	}
}
