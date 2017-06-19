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

package org.springframework.cloud.contract.verifier.util

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ContractVerifierDslConverterSpec extends Specification {

	URL single = ContractVerifierDslConverterSpec.getResource("/contract.groovy")
	File singleContract = new File(single.toURI())
	URL multiple = ContractVerifierDslConverterSpec.getResource("/multiple_contracts.groovy")
	File multipleContracts = new File(multiple.toURI())
	URL invalid = ContractVerifierDslConverterSpec.getResource("/contract.yml")
	File invalidContract = new File(invalid.toURI())

	Contract expectedSingleContract = Contract.make {
		request {
			method('PUT')
			headers {
				contentType(applicationJson())
			}
			body(""" { "status" : "OK" } """)
			url("/1")
		}
		response {
			status 200
			body(""" { "status" : "OK" } """)
			headers {
				contentType(textPlain())
			}
		}
	}

	List<Contract> expectedMultipleContracts = (1..2).collect { int index ->
		Contract.make {
			request {
				method('PUT')
				headers {
					contentType(applicationJson())
				}
				body(""" { "status" : "OK" } """)
				url("/${index}")
			}
			response {
				status 200
				body(""" { "status" : "OK" } """)
				headers {
					contentType(textPlain())
				}
			}
		}
	}

	def "should convert file to a list of Contracts"() {
		when:
			List<Contract> contract = ContractVerifierDslConverter.convertAsCollection(multipleContracts)
		then:
			contract == expectedMultipleContracts
	}

	def "should convert text to a list of Contracts"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(multipleContracts.text)
		then:
			contract == expectedMultipleContracts
	}

	def "should throw an exception when an invalid file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(invalidContract.text)
		then:
			thrown(DslParseException)
	}

	def "should throw an exception with file path when an invalid file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(invalidContract)
		then:
			DslParseException e = thrown(DslParseException)
			e.toString().contains("contract.yml")
	}

	def "should throw an exception when a non existent file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(new File("/foo/bar/baz.foo"))
		then:
			DslParseException e = thrown(DslParseException)
			e.cause instanceof FileNotFoundException
	}

	def "should convert file to a list of Contracts when there's only one declared contract"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(singleContract)
		then:
			contract == [expectedSingleContract]
	}

	def "should convert text to a list of Contracts when there's only one declared contract"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(singleContract.text)
		then:
			contract == [expectedSingleContract]
	}
}
