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

package org.springframework.cloud.contract.verifier.converter

import groovy.json.JsonSlurper
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.cloud.contract.verifier.util.MapConverter

import static org.springframework.cloud.contract.spec.internal.MatchingType.COMMAND
import static org.springframework.cloud.contract.spec.internal.MatchingType.NULL
import static org.springframework.cloud.contract.spec.internal.MatchingType.REGEX
/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 */
class JavaContractConverterSpec extends Specification {

	@Shared
	URL javaRest = JavaContractConverterSpec.
			getResource("/contractsToCompile/contract_rest.java")
	@Shared
	File javaRestFile = new File(javaRest.toURI())

	@Shared
	URL javaRestWithTags = JavaContractConverterSpec.
			getResource("/contractsToCompile/contract_rest_with_tags.java")
	@Shared
	File javaRestWithTagsFile = new File(javaRestWithTags.toURI())

	@Shared
	URL contractBody = JavaContractConverterSpec.
			getResource("/contractsToCompile/contract_rest_from_file.java")
	@Shared
	File contractBodyFile = new File(contractBody.toURI())

	@Shared
	URL contractBodyBytes = JavaContractConverterSpec.
			getResource("/contractsToCompile/contract_rest_from_pdf.java")
	@Shared
	File contractBodyBytesFile = new File(contractBodyBytes.toURI())

	@Shared
	URL docs = JavaContractConverterSpec.
			getResource("/contractsToCompile/contract_docs_examples.java")
	@Shared
	File docsFile = new File(docs.toURI())

	def "should convert Java DSL with REST to DSL for [#contractFile]"() {
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(contractFile.parentFile, contractFile)
		then:
			Contract contract = contracts.first()
			contract.description == "Some description"
			contract.name == "some name"
			contract.priority == 8
			contract.ignored == true
			Url url = contract.request.url
			url.clientValue == "/foo"
			url.queryParameters.parameters[0].name == "a"
			url.queryParameters.parameters[0].serverValue == "b"
			url.queryParameters.parameters[1].name == "b"
			url.queryParameters.parameters[1].serverValue == "c"
			contract.request.method.clientValue == "PUT"
			contract.request.headers.entries.find {
				it.name == "foo" &&
						((RegexProperty) it.clientValue).pattern() == "bar" &&
						it.serverValue == "bar"
			}
			contract.request.headers.entries.find {
				it.name == "fooReq" &&
						it.serverValue == "baz"
			}
			MapConverter.getStubSideValues(contract.request.body) == [foo: "bar"]
			contract.request.bodyMatchers.matchers[0].path() == '$.foo'
			contract.request.bodyMatchers.matchers[0].matchingType() == REGEX
			contract.request.bodyMatchers.matchers[0].value().pattern() == 'bar'
		and:
			contract.response.status.clientValue == 200
			contract.response.delay.clientValue == 1000
			contract.response.headers.entries.find {
				it.name == "foo2" &&
						((RegexProperty) it.serverValue).pattern() == "bar" &&
						it.clientValue == "bar"
			}
			contract.response.headers.entries.find {
				it.name == "foo3" &&
						((ExecutionProperty) it.serverValue).
								insertValue('foo') == "andMeToo(foo)"
			}
			contract.response.headers.entries.find {
				it.name == "fooRes" &&
						it.clientValue == "baz"
			}
			MapConverter.getStubSideValues(
					contract.response.body) == [foo2: "bar", foo3: "baz", nullValue: null]
			contract.response.bodyMatchers.matchers[0].path() == '$.foo2'
			contract.response.bodyMatchers.matchers[0].matchingType() == REGEX
			contract.response.bodyMatchers.matchers[0].value().pattern() == 'bar'
			contract.response.bodyMatchers.matchers[1].path() == '$.foo3'
			contract.response.bodyMatchers.matchers[1].matchingType() == COMMAND
			contract.response.bodyMatchers.matchers[1].
					value() == new ExecutionProperty('executeMe($it)')
			contract.response.bodyMatchers.matchers[2].path() == '$.nullValue'
			contract.response.bodyMatchers.matchers[2].matchingType() == NULL
			contract.response.bodyMatchers.matchers[2].value() == null
		where:
			contractFile << [javaRestFile, javaRestWithTagsFile]
	}

	def "should convert java with REST with body from file"() {
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(contractBodyFile.parentFile, contractBodyFile)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			new JsonSlurper().parseText(contract.request.body.clientValue.toString()) ==
					new JsonSlurper().parseText('''{ "hello" : "request" }''')
		and:
			new JsonSlurper().parseText(contract.response.body.clientValue.toString()) ==
					new JsonSlurper().parseText('''{ "hello" : "response" }''')
	}

	def "should convert java with REST with body as bytes"() {
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(
							contractBodyBytesFile.parentFile, contractBodyBytesFile)
		then:
			contracts.size() == 1
			Contract contract = contracts.first()
			contract.request.body.clientValue instanceof FromFileProperty
		and:
			contract.response.body.clientValue instanceof FromFileProperty
	}

	def "should convert java with REST for docs"() {
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(docsFile.parentFile, docsFile)
		then:
			contracts.size() == 1
	}

	@Issue("1398")
	def "should work when contract starts with 'package'"() {
		given:
			URL packageContract = JavaContractConverterSpec.
					getResource("/contractsToCompile/package_contract.java")
			File packageFile = new File(packageContract.toURI())
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(packageFile.parentFile, packageFile)

		then:
			contracts.size() == 1
	}

	@Issue("1398")
	def "should work when 'contract starts with 'package' preceded by other text"() {
		given:
			URL contract = JavaContractConverterSpec.
					getResource("/contractsToCompile/contract.java")
			File contractFile = new File(contract.toURI())
		when:
			Collection<Contract> contracts = ContractVerifierDslConverter.
					convertAsCollection(contractFile.parentFile, contractFile)

		then:
			contracts.size() == 1
	}
}
