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

import java.nio.file.Files
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import org.yaml.snakeyaml.Yaml

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.MatchingTypeValue
import org.springframework.cloud.contract.verifier.converter.YamlContract.BodyTestMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.BodyStubMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.Input
import org.springframework.cloud.contract.verifier.converter.YamlContract.Request
import org.springframework.cloud.contract.verifier.converter.YamlContract.Response
import org.springframework.cloud.contract.verifier.converter.YamlContract.StubHeaderMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.StubMatchers
import org.springframework.cloud.contract.verifier.converter.YamlContract.StubMatcherType
import org.springframework.cloud.contract.verifier.converter.YamlContract.TestHeaderMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.TestMatcherType
import org.springframework.cloud.contract.verifier.converter.YamlContract.OutputMessage
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Simple converter from and to a {@link YamlContract} to a collection of {@link Contract}
 *
 * @since 1.2.1
 * @author Marcin Grzejszczak
 */
@CompileStatic
class YamlContractConverter implements ContractConverter<List<YamlContract>> {

	public static final YamlContractConverter INSTANCE = new YamlContractConverter()

	@Override
	boolean isAccepted(File file) {
		String name = file.getName()
		return name.endsWith(".yml") || name.endsWith(".yaml")
	}

	@Override
	Collection<Contract> convertFrom(File file) {
		try {
			YamlContract yamlContract = new Yaml().loadAs(
					Files.newInputStream(file.toPath()), YamlContract.class)
			return [Contract.make {
				if (yamlContract.description) description(yamlContract.description)
				if (yamlContract.label) label(yamlContract.label)
				if (yamlContract.name) name(yamlContract.name)
				if (yamlContract.priority) priority(yamlContract.priority)
				if (yamlContract.ignored) ignored()
				if (yamlContract.request?.method) {
					request {
						method(yamlContract.request?.method)
						url(yamlContract.request?.url) {
							queryParameters {
								yamlContract.request.queryParameters.each { String key, String value ->
									parameter(key, value)
								}
							}
						}
						headers {
							yamlContract.request?.headers?.each { String key, Object value ->
								YamlContract.StubHeaderMatcher matcher = yamlContract.request.matchers.headers.find { it.key == key }
								Object clientValue = value
								if (matcher?.regex) {
									clientValue = Pattern.compile(matcher.regex)
									Pattern pattern = (Pattern) clientValue
									boolean matches = pattern.matcher(value.toString()).matches()
									if (!matches) throw new IllegalStateException("Broken request headers! A header with " +
											"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
								}
								header(key, new DslProperty(clientValue, value))
							}
						}
						body(yamlContract.request.body)
						stubMatchers {
							yamlContract.request.matchers?.body?.each { YamlContract.BodyStubMatcher matcher ->
								MatchingTypeValue value = null
								switch (matcher.type) {
									case StubMatcherType.by_date:
										value = byDate()
										break
									case StubMatcherType.by_time:
										value = byTime()
										break
									case StubMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case StubMatcherType.by_regex:
										value = byRegex(matcher.value)
										break
									case StubMatcherType.by_equality:
										value = byEquality()
										break
								}
								jsonPath(matcher.path, value)
							}
						}
					}
					response {
						status(yamlContract.response.status)
						headers {
							yamlContract.response?.headers?.each { String key, Object value ->
								YamlContract.TestHeaderMatcher matcher = yamlContract.response.matchers.headers.find { it.key == key }
								Object serverValue = value
								if (matcher?.regex) {
									serverValue = Pattern.compile(matcher.regex)
									Pattern pattern = (Pattern) serverValue
									boolean matches = pattern.matcher(value.toString()).matches()
									if (!matches) throw new IllegalStateException("Broken response headers! A header with " +
											"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
								} else if (matcher?.command) {
									serverValue = new ExecutionProperty(matcher.command)
								}
								header(key, new DslProperty(value, serverValue))
							}
						}
						if (yamlContract.response.body) body(yamlContract.response.body)
						testMatchers {
							yamlContract.response?.matchers?.body?.each { BodyTestMatcher testMatcher ->
								MatchingTypeValue value = null
								switch (testMatcher.type) {
									case TestMatcherType.by_date:
										value = byDate()
										break
									case TestMatcherType.by_time:
										value = byTime()
										break
									case TestMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case TestMatcherType.by_regex:
										value = byRegex(testMatcher.value)
										break
									case TestMatcherType.by_equality:
										value = byEquality()
										break
									case TestMatcherType.by_type:
										value = byType() {
											minOccurrence(testMatcher.minOccurrence)
											maxOccurrence(testMatcher.maxOccurrence)
										}
										break
									case TestMatcherType.by_command:
										value = byCommand(testMatcher.value)
										break
								}
								jsonPath(testMatcher.path, value)
							}
						}
					}
				}
				if (yamlContract.input) {
					input {
						if (yamlContract.input.messageFrom) messageFrom(yamlContract.input.messageFrom)
						if (yamlContract.input.assertThat) assertThat(yamlContract.input.assertThat)
						if (yamlContract.input.triggeredBy) triggeredBy(yamlContract.input.triggeredBy)
						messageHeaders {
							yamlContract.input?.messageHeaders?.each { String key, Object value ->
								StubHeaderMatcher matcher = yamlContract.input.matchers?.headers?.find { it.key == key }
								Object clientValue = value
								if (matcher?.regex) {
									clientValue = Pattern.compile(matcher.regex)
									Pattern pattern = (Pattern) clientValue
									boolean matches = pattern.matcher(value.toString()).matches()
									if (!matches) throw new IllegalStateException("Broken request headers! A header with " +
											"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
								}
								header(key, new DslProperty(clientValue, value))
							}
						}
						if (yamlContract.input.messageBody) messageBody(yamlContract.input.messageBody)
						stubMatchers {
							yamlContract.input.matchers.body?.each { BodyStubMatcher matcher ->
								MatchingTypeValue value = null
								switch (matcher.type) {
									case StubMatcherType.by_date:
										value = byDate()
										break
									case StubMatcherType.by_time:
										value = byTime()
										break
									case StubMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case StubMatcherType.by_regex:
										value = byRegex(matcher.value)
										break
									case StubMatcherType.by_equality:
										value = byEquality()
										break
								}
								jsonPath(matcher.path, value)
							}
						}
					}
				}
				OutputMessage outputMsg = yamlContract.outputMessage
				if (outputMsg) {
					outputMessage {
						if (outputMsg.assertThat) assertThat(outputMsg.assertThat)
						if (outputMsg.sentTo) sentTo(outputMsg.sentTo)
						headers {
							outputMsg.headers?.each { String key, Object value ->
								TestHeaderMatcher matcher = outputMsg.matchers?.headers?.find { it.key == key }
								Object serverValue = value
								if (matcher?.regex) {
									serverValue = Pattern.compile(matcher.regex)
									Pattern pattern = (Pattern) serverValue
									boolean matches = pattern.matcher(value.toString()).matches()
									if (!matches) throw new IllegalStateException("Broken response headers! A header with " +
											"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
								} else if (matcher?.command) {
									serverValue = new ExecutionProperty(matcher.command)
								}
								header(key, new DslProperty(value, serverValue))
							}
						}
						if (outputMsg.body) body(outputMsg.body)
						if (outputMsg.matchers) {
							testMatchers {
								yamlContract.outputMessage?.matchers?.body?.each { BodyTestMatcher testMatcher ->
									MatchingTypeValue value = null
									switch (testMatcher.type) {
										case TestMatcherType.by_date:
											value = byDate()
											break
										case TestMatcherType.by_time:
											value = byTime()
											break
										case TestMatcherType.by_timestamp:
											value = byTimestamp()
											break
										case TestMatcherType.by_regex:
											value = byRegex(testMatcher.value)
											break
										case TestMatcherType.by_equality:
											value = byEquality()
											break
										case TestMatcherType.by_type:
											value = byType() {
												minOccurrence(testMatcher.minOccurrence)
												maxOccurrence(testMatcher.maxOccurrence)
											}
											break
										case TestMatcherType.by_command:
											value = byCommand(testMatcher.value)
											break
									}
									jsonPath(testMatcher.path, value)
								}
							}
						}
					}
				}
			}]
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e)
		}
	}
	
	@Override
	List<YamlContract> convertTo(Collection<Contract> contracts) {
		return contracts.collect { Contract contract ->
			YamlContract yamlContract = new YamlContract()
			if (contract?.request?.method) {
				yamlContract.request = new Request()
				yamlContract.request.with {
					method = contract?.request?.method?.clientValue
					url = contract?.request?.url?.clientValue
					headers = (contract?.request?.headers as Headers)?.asTestSideMap()
					body = MapConverter.getTestSideValues(contract?.request?.body)
					matchers = new StubMatchers()
					contract?.request?.matchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
						matchers.body << new BodyStubMatcher(
								path: matcher.path(),
								type: stubMatcherType(matcher.matchingType()),
								value: matcher.value()?.toString()
						)
					}
				}
				yamlContract.response = new Response()
				yamlContract.response.with {
					status = contract?.response?.status?.clientValue as Integer
					headers = (contract?.response?.headers as Headers)?.asStubSideMap()
					body = MapConverter.getStubSideValues(contract?.response?.body)
					contract?.response?.matchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
						matchers.body << new BodyTestMatcher(
								path: matcher.path(),
								type: testMatcherType(matcher.matchingType()),
								value: matcher.value()?.toString(),
								minOccurrence: matcher.minTypeOccurrence(),
								maxOccurrence: matcher.maxTypeOccurrence()
						)
					}
				}
			}
			if (contract.input) {
				yamlContract.input = new Input()
				yamlContract.input.assertThat = contract?.input?.assertThat?.toString()
				yamlContract.input.triggeredBy = contract?.input?.triggeredBy?.toString()
				yamlContract.input.messageHeaders = (contract?.input?.messageHeaders as Headers)?.asTestSideMap()
				yamlContract.input.messageBody = MapConverter.getTestSideValues(contract?.input?.messageBody)
				yamlContract.input.messageFrom = contract?.input?.messageFrom?.serverValue
				yamlContract.input.matchers.body.each {
					contract?.input?.matchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
						yamlContract.input.matchers.body << new BodyStubMatcher(
								path: matcher.path(),
								type: stubMatcherType(matcher.matchingType()),
								value: matcher.value()?.toString()
						)
					}
				}
			}
			if (contract.outputMessage) {
				yamlContract.outputMessage = new OutputMessage()
				yamlContract.outputMessage.headers = (contract?.outputMessage?.headers as Headers)?.asStubSideMap()
				yamlContract.outputMessage.body = MapConverter.getStubSideValues(contract?.outputMessage?.body)
				yamlContract.outputMessage.matchers.body.each {
					contract?.input?.matchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
						yamlContract.outputMessage.matchers.body << new BodyTestMatcher(
								path: matcher.path(),
								type: testMatcherType(matcher.matchingType()),
								value: matcher.value()?.toString(),
								minOccurrence: matcher.minTypeOccurrence(),
								maxOccurrence: matcher.maxTypeOccurrence()
						)
					}
				}
			}
			return yamlContract
		}
	}

	protected TestMatcherType testMatcherType(MatchingType matchingType) {
		switch (matchingType) {
			case MatchingType.EQUALITY:
				return TestMatcherType.by_equality
			case MatchingType.TYPE:
				return TestMatcherType.by_type
			case MatchingType.COMMAND:
				return TestMatcherType.by_command
			case MatchingType.DATE:
				return TestMatcherType.by_date
			case MatchingType.TIME:
				return TestMatcherType.by_time
			case MatchingType.TIMESTAMP:
				return TestMatcherType.by_timestamp
			case MatchingType.REGEX:
				return TestMatcherType.by_regex
		}
		return null
	}

	protected StubMatcherType stubMatcherType(MatchingType matchingType) {
		switch (matchingType) {
			case MatchingType.EQUALITY:
				return StubMatcherType.by_equality
			case MatchingType.TYPE:
			case MatchingType.COMMAND:
				throw new UnsupportedOperationException("No type for client side")
			case MatchingType.DATE:
				return StubMatcherType.by_date
			case MatchingType.TIME:
				return StubMatcherType.by_time
			case MatchingType.TIMESTAMP:
				return StubMatcherType.by_timestamp
			case MatchingType.REGEX:
				return StubMatcherType.by_regex
		}
		return null
	}
}
