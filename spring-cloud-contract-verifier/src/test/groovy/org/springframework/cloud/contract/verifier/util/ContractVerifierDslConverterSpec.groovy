/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.contract.verifier.util

import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract

/**
 * @author Marcin Grzejszczak
 */
class ContractVerifierDslConverterSpec extends Specification {

	URL single = ContractVerifierDslConverterSpec.getResource("/contract.groovy")
	File singleContract = new File(single.toURI())
	URL singleJava = ContractVerifierDslConverterSpec.getResource("/contractsToCompile/contract.java")
	File singleContractJava = new File(singleJava.toURI())
	URL singleRestJava = ContractVerifierDslConverterSpec.getResource("/contractsToCompile/contract_rest.java")
	File singleContractRestJava = new File(singleRestJava.toURI())
	URL multiple = ContractVerifierDslConverterSpec.getResource("/multiple_contracts.groovy")
	File multipleContracts = new File(multiple.toURI())
	URL invalid = ContractVerifierDslConverterSpec.getResource("/contract.yml")
	File invalidContract = new File(invalid.toURI())

	Contract expectedSingleContract = Contract.make {
		name("contract")
		request {
			method('PUT')
			headers {
				contentType(applicationJson())
			}
			body(""" { "status" : "OK" } """)
			url("/1")
		}
		response {
			status OK()
			body(""" { "status" : "OK" } """)
			headers {
				contentType(textPlain())
			}
		}
	}

	Contract expectedSingleContractForJava = Contract.make( {
		description("Some description");
		name("some name");
		priority(8);
		ignored();
		request( {
			url("/foo", {
				queryParameters({
					parameter("a", "b");
					parameter("b", "c");
				});
			});
			method(PUT());
			headers( {
				header("foo", value(client(regex("bar")), server("bar")));
				header("fooReq", "baz");
			});
			body(ContractVerifierUtil.map().entry("foo", "bar"));
			bodyMatchers( {
				jsonPath("\$.foo", byRegex("bar"));
			});
		});
		response( {
			fixedDelayMilliseconds(1000);
			status(OK());
			headers( {
				header("foo2", value(server(regex("bar")), client("bar")));
				header("foo3", value(server(execute("andMeToo(\$it)")),
						client("foo33")));
				header("fooRes", "baz");
			});
			body(ContractVerifierUtil.map().entry("foo2", "bar")
									   .entry("foo3", "baz").entry("nullValue", null));
			bodyMatchers( {
				jsonPath("\$.foo2", byRegex("bar"));
				jsonPath("\$.foo3", byCommand("executeMe(\$it)"));
				jsonPath("\$.nullValue", byNull());
			});
		});
	})

	Contract expectedSingleContractForText = Contract.make {
		request {
			method('PUT')
			headers {
				contentType(applicationJson())
			}
			body(""" { "status" : "OK" } """)
			url("/1")
		}
		response {
			status OK()
			body(""" { "status" : "OK" } """)
			headers {
				contentType(textPlain())
			}
		}
	}

	List<Contract> expectedMultipleContracts = (1..2).collect { int index ->
		Contract.make {
			name("multiple_contracts_${index - 1}")
			request {
				method('PUT')
				headers {
					contentType(applicationJson())
				}
				body(""" { "status" : "OK" } """)
				url("/${index}")
			}
			response {
				status OK()
				body(""" { "status" : "OK" } """)
				headers {
					contentType(textPlain())
				}
			}
		}
	}

	List<Contract> expectedMultipleContractsForText = (1..2).collect { int index ->
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
				status OK()
				body(""" { "status" : "OK" } """)
				headers {
					contentType(textPlain())
				}
			}
		}
	}

	def "should convert file to a list of Contracts"() {
		when:
			List<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), multipleContracts)
		then:
			contract == expectedMultipleContracts
	}

	def "should convert text to a list of Contracts"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), multipleContracts.text)
		then:
			contract == expectedMultipleContractsForText
	}

	def "should throw an exception when an invalid file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(new File("/"), invalidContract.text)
		then:
			thrown(DslParseException)
	}

	def "should throw an exception with file path when an invalid file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(new File("/"), invalidContract)
		then:
			DslParseException e = thrown(DslParseException)
			e.toString().contains("contract.yml")
	}

	def "should throw an exception when a non existent file is parsed"() {
		when:
			ContractVerifierDslConverter.convertAsCollection(new File("/"), new File("/foo/bar/baz.foo"))
		then:
			DslParseException e = thrown(DslParseException)
			e.cause instanceof FileNotFoundException
	}

	def "should convert file to a list of Contracts when there's only one declared contract"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), singleContract)
		then:
			contract == [expectedSingleContract]
	}

	def "should convert file to a list of Contracts when there's only one declared java contract"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), singleContractJava)
		then:
			contract == [expectedSingleContract]
	}

	def "should convert file to a list of Contracts for a REST contract for docs"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), singleContractRestJava)
		then:
			contract == [expectedSingleContractForJava]
	}

	def "should convert text to a list of Contracts when there's only one declared contract"() {
		when:
			Collection<Contract> contract = ContractVerifierDslConverter.convertAsCollection(new File("/"), singleContract.text)
		then:
			contract == [expectedSingleContractForText]
	}
}
