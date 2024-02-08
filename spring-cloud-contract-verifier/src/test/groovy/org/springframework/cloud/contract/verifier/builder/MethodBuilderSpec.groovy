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

package org.springframework.cloud.contract.verifier.builder


import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata

class MethodBuilderSpec extends Specification {

	@Issue('#518')
	def "should map create valid method name from file name containing illegal chars"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/foo'
				}
				response {
					status OK()
					body(foo: "foo")
					headers {
						contentType(applicationJson())
					}
				}
			}
			File stubFile = new File("invalid-method;name.groovy")

			ContractMetadata metadata = new ContractMetadata(stubFile.toPath(), false, 0, null, contractDsl)
			SingleContractMetadata singleContractMetadata = new SingleContractMetadata(contractDsl, metadata)
		when:

			String methodName = new NameProvider().methodName(singleContractMetadata)
		then:
			methodName == "validate_invalid_method_name"
	}

}
