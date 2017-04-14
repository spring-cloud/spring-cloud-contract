/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder

import org.junit.Rule
import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.dsl.WireMockStubVerifier
import org.springframework.cloud.contract.verifier.util.SyntaxChecker
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class MethodBodyBuilderSpec extends Specification implements WireMockStubVerifier {

	@Rule OutputCapture capture = new OutputCapture()

	@Shared ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(
			assertJsonSize: true
	)

	@Issue('#251')
	def "should work with execute and arrays [#methodBuilderName]"() {
		given:
		Contract contractDsl = Contract.make {
			request {
				method 'GET'
				urlPath '/foo'
				headers {
					accept(applicationJson())
					contentType(applicationJson())
				}
			}
			response {
				status 200
				body ([
						myArray:[
								[
										notABugGeneratedHere: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("foo")'))),
										anotherArrayNeededForBug:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("12")')))
												]
										],
										yetAnotherArrayNeededForBug:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("22")')))
												]
										]
								],
								[
										anotherArrayNeededForBug2:[
												[
														optionalNotEmpty: $(c("foo"), p(execute('assertThat((String)$it).isEqualTo("122")')))
												]
										]
								],
						]
				])
				headers {
					contentType(applicationJson())
				}
			}
		}
			MethodBodyBuilder builder = methodBuilder(contractDsl)
			BlockBuilder blockBuilder = new BlockBuilder(" ")
		when:
			builder.appendTo(blockBuilder)
			def test = blockBuilder.toString()
		then:
			test.contains('$.myArray[0].anotherArrayNeededForBug[0].optionalNotEmpty')
			!test.contains('cursor')
			!test.contains('REGEXP>>')
		and:
			SyntaxChecker.tryToCompile(methodBuilderName, blockBuilder.toString())
		and:
			String jsonSample = '''\
String json = "{\\"myArray\\":[{\\"notABugGeneratedHere\\":\\"foo\\",\\"anotherArrayNeededForBug\\":[{\\"optionalNotEmpty\\":\\"12\\"}],\\"yetAnotherArrayNeededForBug\\":[{\\"optionalNotEmpty\\":\\"22\\"}]},{\\"anotherArrayNeededForBug2\\":[{\\"optionalNotEmpty\\":\\"122\\"}]}]}";
DocumentContext parsedJson = JsonPath.parse(json);
'''
		and:
			LinkedList<String> lines = [] as LinkedList<String>
			test.eachLine { if (it.contains("assertThatJson") || it.contains("assertThat((String")) lines << it else it }
			lines.addFirst(jsonSample)
			SyntaxChecker.tryToRun(methodBuilderName, lines.join("\n"))
		where:
			methodBuilderName                                    | methodBuilder
			"MockMvcSpockMethodBuilder"                          | { Contract dsl -> new MockMvcSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"MockMvcJUnitMethodBuilder"                          | { Contract dsl -> new MockMvcJUnitMethodBodyBuilder(dsl, properties) }
			"JaxRsClientSpockMethodRequestProcessingBodyBuilder" | { Contract dsl -> new JaxRsClientSpockMethodRequestProcessingBodyBuilder(dsl, properties) }
			"JaxRsClientJUnitMethodBodyBuilder"                  | { Contract dsl -> new JaxRsClientJUnitMethodBodyBuilder(dsl, properties) }
	}

}
