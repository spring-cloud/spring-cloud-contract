/*
 *  Copyright 2013-2018 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.verifier.converter.YamlContract.BodyStubMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.BodyTestMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.Input
import org.springframework.cloud.contract.verifier.converter.YamlContract.Named
import org.springframework.cloud.contract.verifier.converter.YamlContract.OutputMessage
import org.springframework.cloud.contract.verifier.converter.YamlContract.PredefinedRegex
import org.springframework.cloud.contract.verifier.converter.YamlContract.Request
import org.springframework.cloud.contract.verifier.converter.YamlContract.Response
import org.springframework.cloud.contract.verifier.converter.YamlContract.KeyValueMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.StubMatcherType
import org.springframework.cloud.contract.verifier.converter.YamlContract.StubMatchers
import org.springframework.cloud.contract.verifier.converter.YamlContract.TestHeaderMatcher
import org.springframework.cloud.contract.verifier.converter.YamlContract.TestMatcherType
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Simple converter from and to a {@link YamlContract} to a collection of {@link Contract}
 *
 * @since 1.2.1
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
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
	Collection<Contract> convertFrom(File contractFile) {
		ClassLoader classLoader = YamlContractConverter.getClassLoader()
		ObjectMapper mapper = new ObjectMapper()
		try {
			return new Yaml().loadAll(Files.newInputStream(contractFile.toPath())).collect {
				YamlContract yamlContract = mapper.convertValue(it, YamlContract.class);
				Thread.currentThread().setContextClassLoader(updatedClassLoader(contractFile.getParentFile(), classLoader))
				return Contract.make {
					if (yamlContract.description) description(yamlContract.description)
					if (yamlContract.label) label(yamlContract.label)
					if (yamlContract.name) name(yamlContract.name)
					if (yamlContract.priority) priority(yamlContract.priority)
					if (yamlContract.ignored) ignored()
					if (yamlContract.request?.method) {
						request {
							method(yamlContract.request?.method)
							if (yamlContract.request?.url) {
								url(yamlContract.request?.url) {
									if (yamlContract.request.queryParameters) {
										queryParameters {
											yamlContract.request.queryParameters.each { String key, Object value ->
												if (value instanceof List) {
													((List) value).each {
														parameter(key, it)
													}
												} else {
													parameter(key, value)
												}
											}
										}
									}
								}
							}
							if (yamlContract.request?.urlPath) {
								urlPath(yamlContract.request?.urlPath) {
									if (yamlContract.request.queryParameters) {
										queryParameters {
											yamlContract.request.queryParameters.each { String key, Object value ->
												if (value instanceof List) {
													((List) value).each {
														parameter(key, it)
													}
												} else {
													parameter(key, value)
												}
											}
										}
									}
								}
							}
							if (yamlContract.request?.headers) {
								headers {
									yamlContract.request?.headers?.each { String key, Object value ->
										KeyValueMatcher matcher = yamlContract.request.matchers.headers.find { it.key == key }
										if (value instanceof List) {
											((List) value).each {
												Object clientValue = clientValue(it, matcher, key)
												header(key, new DslProperty(clientValue, it))
											}
										} else {
											Object clientValue = clientValue(value, matcher, key)
											header(key, new DslProperty(clientValue, value))
										}
									}
								}
							}
							if (yamlContract.request.body) body(yamlContract.request.body)
							if (yamlContract.request.bodyFromFile) body(file(yamlContract.request.bodyFromFile))
							if (yamlContract.request.multipart) {
								Map multipartMap = [:]
								Map<String, DslProperty> multiPartParams = yamlContract.request.multipart.params.collectEntries { String paramKey, String paramValue ->
									KeyValueMatcher matcher = yamlContract.request.matchers.multipart.params.find {
										it.key == paramKey
									}
									Object value = paramValue
									if (matcher) {
										value = matcher.regex ? Pattern.compile(matcher.regex) :
												predefinedToPattern(matcher.predefined)
									}
									return [(paramKey), new DslProperty<>(value, paramValue)]
								} as Map<String, DslProperty>
								multipartMap.putAll(multiPartParams)
								yamlContract.request.multipart.named.each { Named namedParam ->
									YamlContract.MultipartNamedStubMatcher matcher = yamlContract.request.matchers.multipart.named.find {
										it.paramName == namedParam.paramName
									}
									Object fileNameValue = namedParam.fileName
									Object fileContentValue = namedParam.fileContent
									if (matcher && matcher.fileName) {
										fileNameValue = matcher.fileName.regex ? Pattern.compile(matcher.fileName.regex) :
												predefinedToPattern(matcher.fileName.predefined)
									}
									if (matcher && matcher.fileContent) {
										fileContentValue = matcher.fileContent.regex ? Pattern.compile(matcher.fileContent.regex) :
												predefinedToPattern(matcher.fileContent.predefined)
									}
									multipartMap.put(namedParam.paramName, new NamedProperty(new DslProperty<>(fileNameValue, namedParam.fileName),
											new DslProperty<>(fileContentValue, namedParam.fileContent)))
								}
								multipart(multipartMap)
							}
							bodyMatchers {
								yamlContract.request.matchers?.body?.each { BodyStubMatcher matcher ->
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
											String regex = matcher.value
											if (matcher.predefined) {
												regex = predefinedToPattern(matcher.predefined).pattern()
											}
											value = byRegex(regex)
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
									TestHeaderMatcher matcher = yamlContract.response.matchers.headers.find { it.key == key }
									if (value instanceof List) {
										((List) value).each {
											Object serverValue = serverValue(it, matcher, key)
											header(key, new DslProperty(it, serverValue))
										}
									} else {
										Object serverValue = serverValue(value, matcher, key)
										header(key, new DslProperty(value, serverValue))
									}
								}
							}
							if (yamlContract.response.body) body(yamlContract.response.body)
							if (yamlContract.response.bodyFromFile) body(file(yamlContract.response.bodyFromFile))
							if (yamlContract.response.async) async()
							bodyMatchers {
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
											String regex = testMatcher.value
											if (testMatcher.predefined) {
												regex = predefinedToPattern(testMatcher.predefined).pattern()
											}
											value = byRegex(regex)
											break
										case TestMatcherType.by_equality:
											value = byEquality()
											break
										case TestMatcherType.by_type:
											value = byType() {
												if (testMatcher.minOccurrence != null) minOccurrence(testMatcher.minOccurrence)
												if (testMatcher.maxOccurrence != null) maxOccurrence(testMatcher.maxOccurrence)
											}
											break
										case TestMatcherType.by_command:
											value = byCommand(testMatcher.value)
											break
										case TestMatcherType.by_null:
											value = byNull()
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
									KeyValueMatcher matcher = yamlContract.input.matchers?.headers?.find { it.key == key }
									Object clientValue = clientValue(value, matcher, key)
									header(key, new DslProperty(clientValue, value))
								}
							}
							if (yamlContract.input.messageBody) messageBody(yamlContract.input.messageBody)
							if (yamlContract.input.messageBodyFromFile) messageBody(file(yamlContract.input.messageBodyFromFile))
							bodyMatchers {
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
											String regex = matcher.value
											if (matcher.predefined) {
												regex = predefinedToPattern(matcher.predefined).pattern()
											}
											value = byRegex(regex)
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
									Object serverValue = serverValue(value, matcher, key)
									header(key, new DslProperty(value, serverValue))
								}
							}
							if (outputMsg.body) body(outputMsg.body)
							if (outputMsg.bodyFromFile) body(file(outputMsg.bodyFromFile))
							if (outputMsg.matchers) {
								bodyMatchers {
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
												String regex = testMatcher.value
												if (testMatcher.predefined) {
													regex = predefinedToPattern(testMatcher.predefined).pattern()
												}
												value = byRegex(regex)
												break
											case TestMatcherType.by_equality:
												value = byEquality()
												break
											case TestMatcherType.by_type:
												value = byType() {
													if (testMatcher.minOccurrence != null) minOccurrence(testMatcher.minOccurrence)
													if (testMatcher.maxOccurrence != null) maxOccurrence(testMatcher.maxOccurrence)
												}
												break
											case TestMatcherType.by_command:
												value = byCommand(testMatcher.value)
												break
											case TestMatcherType.by_null:
												value = byNull()
												break
										}
										jsonPath(testMatcher.path, value)
									}
								}
							}
						}
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e)
		}
		catch (IllegalStateException ise) {
			throw ise
		}
		catch (Exception e1) {
			throw new IllegalStateException("Exception occurred while processing the file [" + contractFile + "]", e1)
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	protected Object serverValue(Object value, TestHeaderMatcher matcher, String key) {
		Object serverValue = value
		if (matcher?.regex) {
			serverValue = Pattern.compile(matcher.regex)
			Pattern pattern = (Pattern) serverValue
			assertPatternMatched(pattern, value, key)
		} else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			serverValue = pattern
			assertPatternMatched(pattern, value, key)
		} else if (matcher?.command) {
			serverValue = new ExecutionProperty(matcher.command)
		}
		return serverValue
	}

	protected Object clientValue(Object value, KeyValueMatcher matcher, String key) {
		Object clientValue = value
		if (matcher?.regex) {
			clientValue = Pattern.compile(matcher.regex)
			Pattern pattern = (Pattern) clientValue
			assertPatternMatched(pattern, value, key)
		} else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			clientValue = pattern
			assertPatternMatched(pattern, value, key)
		}
		return clientValue
	}

	private void assertPatternMatched(Pattern pattern, value, String key) {
		boolean matches = pattern.matcher(value.toString()).matches()
		if (!matches) throw new IllegalStateException("Broken headers! A header with " +
				"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
	}

	protected Pattern predefinedToPattern(PredefinedRegex predefinedRegex) {
		RegexPatterns patterns = new RegexPatterns()
		switch (predefinedRegex) {
			case PredefinedRegex.only_alpha_unicode:
				return patterns.onlyAlphaUnicode()
			case PredefinedRegex.number:
				return patterns.number()
			case PredefinedRegex.any_double:
				return patterns.aDouble()
			case PredefinedRegex.any_boolean:
				return patterns.anyBoolean()
			case PredefinedRegex.ip_address:
				return patterns.ipAddress()
			case PredefinedRegex.hostname:
				return patterns.hostname()
			case PredefinedRegex.email:
				return patterns.email()
			case PredefinedRegex.url:
				return patterns.url()
			case PredefinedRegex.uuid:
				return patterns.uuid()
			case PredefinedRegex.iso_date:
				return patterns.isoDate()
			case PredefinedRegex.iso_date_time:
				return patterns.isoDateTime()
			case PredefinedRegex.iso_time:
				return patterns.isoTime()
			case PredefinedRegex.iso_8601_with_offset:
				return patterns.iso8601WithOffset()
			case PredefinedRegex.non_empty:
				return patterns.nonEmpty()
			case PredefinedRegex.non_blank:
				return patterns.nonBlank()
		}
	}

	protected String file(String relativePath) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(relativePath)
		if (resource == null) {
			throw new IllegalStateException("File [${relativePath}] is not present")
		}
		return new File(resource.toURI()).text
	}

	protected static ClassLoader updatedClassLoader(File rootFolder, ClassLoader classLoader) {
		ClassLoader urlCl = URLClassLoader
				.newInstance([rootFolder.toURI().toURL()] as URL[], classLoader)
		Thread.currentThread().setContextClassLoader(urlCl)
		return urlCl
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
					contract?.request?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
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
					contract?.response?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
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
					contract?.input?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
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
					contract?.input?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
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
