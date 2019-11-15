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

package org.springframework.cloud.contract.verifier.converter

import java.nio.file.Files
import java.util.regex.Pattern

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.yaml.snakeyaml.Yaml

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingTypeValue
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.NamesUtil
import org.springframework.util.StringUtils

import static java.util.stream.Collectors.toSet
import static org.springframework.cloud.contract.verifier.util.ContentType.XML
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateContentType

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 */
@CompileStatic
@PackageScope
class YamlToContracts {

	Collection<Contract> convertFrom(File contractFile) {
		ClassLoader classLoader = YamlContractConverter.getClassLoader()
		YAMLMapper mapper = new YAMLMapper()
		try {
			Iterable<Object> iterables = new Yaml().
				loadAll(Files.newInputStream(contractFile.toPath()))
			Collection<Contract> contracts = []
			int counter = 0
			for (Object document : iterables) {
				List<Contract> processedYaml =
					processYaml(counter, document, mapper, classLoader, contractFile)
				contracts.addAll(processedYaml)
				counter = counter + 1
			}
			return contracts
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e)
		}
		catch (IllegalStateException ise) {
			throw ise
		}
		catch (Exception e1) {
			throw new IllegalStateException("Exception occurred while processing the file [" + contractFile + "]", e1)
		}
		finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	protected List<Contract> processYaml(int counter, Object document, ObjectMapper mapper, ClassLoader classLoader, File contractFile) {
		List<YamlContract> yamlContracts = convert(mapper, document)
		Thread.currentThread().setContextClassLoader(
			updatedClassLoader(contractFile.getParentFile(), classLoader))
		List<Contract> contracts = []
		for (YamlContract yamlContract : yamlContracts) {
			Contract contract = Contract.make {
				if (yamlContract.description) {
					description(yamlContract.description)
				}
				if (yamlContract.label) {
					label(yamlContract.label)
				}
				name(StringUtils.hasText(yamlContract.name) ? yamlContract.name
					: NamesUtil.defaultContractName(contractFile, yamlContracts, counter))
				if (yamlContract.priority) {
					priority(yamlContract.priority)
				}
				if (yamlContract.ignored) {
					ignored()
				}
				if (yamlContract.request?.method) {
					request {
						method(yamlContract.request?.method)
						if (yamlContract.request?.url) {
							url(urlValue(yamlContract.request?.url, yamlContract.request?.matchers?.url)) {
								if (yamlContract.request.queryParameters) {
									queryParameters {
										yamlContract.request.queryParameters.
											each { String key, Object value ->
												if (value instanceof List) {
													((List) value).each {
														parameter(key, it)
													}
												}
												else {
													parameter(key, value)
												}
											}
									}
								}
							}
						}
						if (yamlContract.request?.urlPath) {
							urlPath(
								urlValue(yamlContract.request?.urlPath, yamlContract.request?.matchers?.url)) {
								if (yamlContract.request.queryParameters) {
									queryParameters {
										yamlContract.request.queryParameters.
											each { String key, Object value ->
												if (value instanceof List) {
													((List) value).each {
														parameter(key,
															queryParamValue(yamlContract, key, it))
													}
												}
												else {
													parameter(key,
														queryParamValue(yamlContract, key, value))
												}
											}
									}
								}
							}
						}
						if (yamlContract.request?.headers) {
							headers {
								yamlContract.request.headers.
									each { String key, Object value ->
										List<YamlContract.KeyValueMatcher> matchers =
											yamlContract.request.matchers.headers.
												findAll { it.key == key }
										matchers.
											each { YamlContract.KeyValueMatcher matcher ->
												if (value instanceof List) {
													((List) value).each {
														header(key,
															clientValue(it, matcher, key).clientValue)
													}
												}
												else {
													header(key, new DslProperty(
														clientValue(value, matcher, key).clientValue,
														serverValue(value, matcher)))
												}
											}
										if (!matchers) {
											header(key, value)
										}
									}
							}
						}
						if (yamlContract.request?.cookies) {
							cookies {
								yamlContract.request?.cookies?.
									each { String key, Object value ->
										YamlContract.KeyValueMatcher matcher = yamlContract.request.matchers.cookies.
											find { it.key == key }
										cookie(key, clientValue(value, matcher, key))
									}
							}
						}
						if (yamlContract.request.body != null) {
							body(yamlContract.request.body)
						}
						if (yamlContract.request.bodyFromFile != null) {
							body(file(yamlContract.request.bodyFromFile))
						}
						if (yamlContract.request.bodyFromFileAsBytes != null) {
							body(fileAsBytes(yamlContract.request.bodyFromFileAsBytes))
						}
						if (yamlContract.request.multipart) {
							Map multipartMap = [:]
							Map<String, DslProperty> multiPartParams = yamlContract.request
								.multipart.params.
								collectEntries { String paramKey, String paramValue ->
									YamlContract.KeyValueMatcher matcher = yamlContract.request.matchers
										.multipart.params.find {
										it.key == paramKey
									}
									Object value = paramValue
									if (matcher) {
										value = matcher.regex ? Pattern.
											compile(matcher.regex) :
											predefinedToPattern(matcher.predefined)
									}
									return [(paramKey), new DslProperty<>(value, paramValue)]
								} as Map<String, DslProperty>
							multipartMap.putAll(multiPartParams)
							yamlContract.request.multipart.named.
								each { YamlContract.Named namedParam ->
									YamlContract.MultipartNamedStubMatcher matcher = yamlContract.request.matchers.multipart.named.
										find {
											it.paramName == namedParam.paramName
										}
									Object fileNameValue = namedParam.fileName
									Object fileContentValue = namedParam.fileContent
									String fileContentAsBytes = namedParam.fileContentAsBytes
									String fileContentFromFileAsBytes = namedParam.fileContentFromFileAsBytes
									String contentTypeCommand = namedParam.contentTypeCommand
									String fileContentCommand = namedParam.fileContentCommand
									String fileNameCommand = namedParam.fileNameCommand
									Object contentTypeValue = namedParam.contentType
									if (matcher && matcher.fileName) {
										fileNameValue = matcher.fileName.regex ? Pattern.
											compile(matcher.fileName.regex) :
											predefinedToPattern(matcher.fileName.predefined)
									}
									if (matcher && matcher.fileContent) {
										fileContentValue = matcher.fileContent.regex ? Pattern.
											compile(matcher.fileContent.regex) :
											predefinedToPattern(matcher.fileContent.predefined)
									}
									if (matcher && matcher.contentType) {
										contentTypeValue = matcher.contentType.regex ? Pattern.
											compile(matcher.contentType.regex) :
											predefinedToPattern(matcher.contentType.predefined)
									}
									multipartMap.
										put(namedParam.paramName, new NamedProperty(
											new DslProperty<>(fileNameValue, fileNameCommand ? new ExecutionProperty(fileNameCommand)
												: namedParam.fileName),
											new DslProperty<>(fileContentValue, namedParam.fileContent ? namedParam.fileContent : fileContentFromFileAsBytes ?
												fileAsBytes(namedParam.fileContentFromFileAsBytes) : fileContentAsBytes ? fileContentAsBytes.bytes : new ExecutionProperty(fileContentCommand)),
											new DslProperty(contentTypeValue, contentTypeCommand ? new ExecutionProperty(contentTypeCommand)
												: namedParam.contentType)))
								}
							multipart(multipartMap)
						}
						bodyMatchers {
							yamlContract.request.matchers?.body?.
								each { YamlContract.BodyStubMatcher matcher ->
									ContentType contentType =
										evaluateContentType(
											yamlHeadersToContractHeaders(yamlContract.request?.headers),
											yamlContract.request?.body)
									MatchingTypeValue value = null
									switch (matcher.type) {
									case YamlContract.StubMatcherType.by_date:
										value = byDate()
										break
									case YamlContract.StubMatcherType.by_time:
										value = byTime()
										break
									case YamlContract.StubMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case YamlContract.StubMatcherType.by_regex:
										String regex = matcher.value
										if (matcher.predefined) {
											regex =
												predefinedToPattern(matcher.predefined).
													pattern()
										}
										value = byRegex(regex)
										break
									case YamlContract.StubMatcherType.by_equality:
										value = byEquality()
										break
									case YamlContract.StubMatcherType.by_type:
										value = byType {
											if (matcher.minOccurrence != null) {
												minOccurrence(matcher.minOccurrence)
											}
											if (matcher.maxOccurrence != null) {
												maxOccurrence(matcher.maxOccurrence)
											}
										}
										break
									case YamlContract.StubMatcherType.by_null:
										// do nothing
										break
									default:
										throw new UnsupportedOperationException("The type [" + matcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
									}
									if (value) {
										if (XML == contentType) {
											xPath(matcher.path, value)
										}
										else {
											jsonPath(matcher.path, value)
										}
									}
								}
						}
					}
					response {
						status(yamlContract.response.status)
						headers {
							yamlContract.response?.headers?.
								each { String key, Object value ->
									YamlContract.TestHeaderMatcher matcher = yamlContract.response.matchers.headers.
										find { it.key == key }
									if (value instanceof List) {
										((List) value).each {
											Object serverValue =
												serverValue(it, matcher, key)
											header(key, new DslProperty(it, serverValue))
										}
									}
									else {
										Object serverValue =
											serverValue(value, matcher, key)
										header(key, new DslProperty(value, serverValue))
									}
								}
						}
						if (yamlContract.response?.cookies) {
							cookies {
								yamlContract.response?.cookies?.
									each { String key, Object value ->
										YamlContract.TestCookieMatcher matcher = yamlContract.response.matchers.cookies.
											find { it.key == key }
										DslProperty cookieValue =
											serverCookieValue(value, matcher, key)
										cookie(key, cookieValue)
									}
							}
						}
						if (yamlContract.response.body != null) {
							YamlContract.BodyTestMatcher bodyTestMatcher = yamlContract.response?.matchers?.body?.find {
								it.path == null && (it.type == YamlContract.TestMatcherType.by_regex ||
										it.type == YamlContract.TestMatcherType.by_command)
								}
							if (bodyTestMatcher) {
								body(new DslProperty(yamlContract.response.body,
									bodyTestMatcher.type == YamlContract.TestMatcherType.by_regex ?
										Pattern.
											compile(bodyTestMatcher.value) : new ExecutionProperty(bodyTestMatcher.value)))
							}
							else {
								body(yamlContract.response.body)
							}
						}
						if (yamlContract.response.bodyFromFile) {
							body(file(yamlContract.response.bodyFromFile))
						}
						if (yamlContract.response.bodyFromFileAsBytes) {
							body(fileAsBytes(yamlContract.response.bodyFromFileAsBytes))
						}
						if (yamlContract.response.async) {
							async()
						}
						if (yamlContract.response.fixedDelayMilliseconds) {
							async()
							fixedDelayMilliseconds(yamlContract.response.fixedDelayMilliseconds)
						}
						bodyMatchers {
							yamlContract.response?.matchers?.body?.
								each { YamlContract.BodyTestMatcher testMatcher ->
									ContentType contentType =
										evaluateContentType(
											yamlHeadersToContractHeaders(yamlContract.response?.headers),
											yamlContract.response?.body)
									MatchingTypeValue value = null
									switch (testMatcher.type) {
									case YamlContract.TestMatcherType.by_date:
										value = byDate()
										break
									case YamlContract.TestMatcherType.by_time:
										value = byTime()
										break
									case YamlContract.TestMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case YamlContract.TestMatcherType.by_regex:
										String regex = testMatcher.value
										if (testMatcher.predefined) {
											regex =
												predefinedToPattern(testMatcher.predefined).
													pattern()
										}
										value = byRegex(regex)
										break
									case YamlContract.TestMatcherType.by_equality:
										value = byEquality()
										break
									case YamlContract.TestMatcherType.by_type:
										value = byType() {
											if (testMatcher.minOccurrence != null) {
												minOccurrence(testMatcher.minOccurrence)
											}
											if (testMatcher.maxOccurrence != null) {
												maxOccurrence(testMatcher.maxOccurrence)
											}
										}
										break
									case YamlContract.TestMatcherType.by_command:
										value = byCommand(testMatcher.value)
										break
									case YamlContract.TestMatcherType.by_null:
										value = byNull()
										break
									default:
										throw new UnsupportedOperationException("The type [" + testMatcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
									}
									if (testMatcher.path) {
										if (XML == contentType) {
											xPath(testMatcher.path, value)
										}
										else {
											jsonPath(testMatcher.path, value)
										}
									}
								}
						}
					}
				}
				if (yamlContract.input) {
					input {
						if (yamlContract.input.messageFrom) {
							messageFrom(yamlContract.input.messageFrom)
						}
						if (yamlContract.input.assertThat) {
							assertThat(yamlContract.input.assertThat)
						}
						if (yamlContract.input.triggeredBy) {
							triggeredBy(yamlContract.input.triggeredBy)
						}
						messageHeaders {
							yamlContract.input?.messageHeaders?.
								each { String key, Object value ->
									YamlContract.KeyValueMatcher matcher = yamlContract.input.matchers?.headers?.
										find { it.key == key }
									header(key, clientValue(value, matcher, key))
								}
						}
						if (yamlContract.input.messageBody) {
							messageBody(yamlContract.input.messageBody)
						}
						if (yamlContract.input.messageBodyFromFile) {
							messageBody(file(yamlContract.input.messageBodyFromFile))
						}
						if (yamlContract.input.messageBodyFromFileAsBytes) {
							messageBody(
								fileAsBytes(yamlContract.input.messageBodyFromFileAsBytes))
						}
						bodyMatchers {
							yamlContract.input.matchers.body?.
								each { YamlContract.BodyStubMatcher matcher ->
									ContentType contentType =
										evaluateContentType(
											yamlHeadersToContractHeaders(yamlContract.input?.messageHeaders),
											yamlContract.input?.messageBody)
									MatchingTypeValue value = null
									switch (matcher.type) {
									case YamlContract.StubMatcherType.by_date:
										value = byDate()
										break
									case YamlContract.StubMatcherType.by_time:
										value = byTime()
										break
									case YamlContract.StubMatcherType.by_timestamp:
										value = byTimestamp()
										break
									case YamlContract.StubMatcherType.by_regex:
										String regex = matcher.value
										if (matcher.predefined) {
											regex =
												predefinedToPattern(matcher.predefined).
													pattern()
										}
										value = byRegex(regex)
										break
									case YamlContract.StubMatcherType.by_equality:
										value = byEquality()
										break
									default:
										throw new UnsupportedOperationException("The type [" + matcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
									}
									if (XML == contentType) {
										xPath(matcher.path, value)
									}
									else {
										jsonPath(matcher.path, value)
									}
								}
						}
					}
				}
				YamlContract.OutputMessage outputMsg = yamlContract.outputMessage
				if (outputMsg) {
					outputMessage {
						if (outputMsg.assertThat) {
							assertThat(outputMsg.assertThat)
						}
						if (outputMsg.sentTo) {
							sentTo(outputMsg.sentTo)
						}
						headers {
							outputMsg.headers?.each { String key, Object value ->
								YamlContract.TestHeaderMatcher matcher = outputMsg.matchers?.headers?.
									find { it.key == key }
								Object serverValue = serverValue(value, matcher, key)
								header(key, new DslProperty(value, serverValue))
							}
						}
						if (outputMsg.body) {
							body(outputMsg.body)
						}
						if (outputMsg.bodyFromFile) {
							body(file(outputMsg.bodyFromFile))
						}
						if (outputMsg.bodyFromFileAsBytes) {
							body(fileAsBytes(outputMsg.bodyFromFileAsBytes))
						}
						if (outputMsg.matchers) {
							bodyMatchers {
								yamlContract.outputMessage?.matchers?.body?.
									each { YamlContract.BodyTestMatcher testMatcher ->
										ContentType contentType =
											evaluateContentType(
												yamlHeadersToContractHeaders(yamlContract.outputMessage?.headers),
												yamlContract.outputMessage?.body)
										MatchingTypeValue value = null
										switch (testMatcher.type) {
										case YamlContract.TestMatcherType.by_date:
											value = byDate()
											break
										case YamlContract.TestMatcherType.by_time:
											value = byTime()
											break
										case YamlContract.TestMatcherType.by_timestamp:
											value = byTimestamp()
											break
										case YamlContract.TestMatcherType.by_regex:
											String regex = testMatcher.value
											if (testMatcher.predefined) {
												regex =
													predefinedToPattern(testMatcher.predefined).
														pattern()
											}
											value = byRegex(regex)
											break
										case YamlContract.TestMatcherType.by_equality:
											value = byEquality()
											break
										case YamlContract.TestMatcherType.by_type:
											value = byType() {
												if (testMatcher.minOccurrence != null) {
													minOccurrence(testMatcher.minOccurrence)
												}
												if (testMatcher.maxOccurrence != null) {
													maxOccurrence(testMatcher.maxOccurrence)
												}
											}
											break
										case YamlContract.TestMatcherType.by_command:
											value = byCommand(testMatcher.value)
											break
										case YamlContract.TestMatcherType.by_null:
											value = byNull()
											break
										default:
											throw new UnsupportedOperationException("The type [" + testMatcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
										}
										if (XML == contentType) {
											xPath(testMatcher.path, value)
										}
										else {
											jsonPath(testMatcher.path, value)
										}
									}
							}
						}
					}
				}
			}
			contracts.add(contract)
		}
		return contracts
	}

	private Headers yamlHeadersToContractHeaders(Map<String, Object> headers) {
		Set<Header> convertedHeaders = headers.keySet().stream()
			.map({ new Header(it, headers.get(it)) })
			.collect(toSet())
		Headers contractHeaders = new Headers()
		contractHeaders.headers(convertedHeaders)
		return contractHeaders
	}

	protected DslProperty urlValue(String url, YamlContract.KeyValueMatcher urlMatcher) {
		if (urlMatcher) {
			if (urlMatcher.command) {
				return new DslProperty<Object>(url, new ExecutionProperty(urlMatcher.command))
			}
			return new DslProperty(urlMatcher.regex ? Pattern.compile(urlMatcher.regex) :
				urlMatcher.predefined ?
					predefinedToPattern(urlMatcher.predefined) : url, url)
		}
		return new DslProperty(url)
	}

	protected List<YamlContract> convert(ObjectMapper mapper, Object o) {
		try {
			return Arrays.asList(mapper.convertValue(o, YamlContract[].class))
		}
		catch (IllegalArgumentException e) {
			return Collections.singletonList(mapper.convertValue(o, YamlContract.class))
		}
	}

	protected Object serverValue(Object value, YamlContract.TestHeaderMatcher matcher, String key) {
		Object serverValue = value
		if (matcher?.regex) {
			serverValue = Pattern.compile(matcher.regex)
			Pattern pattern = (Pattern) serverValue
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			serverValue = pattern
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.command) {
			serverValue = new ExecutionProperty(matcher.command)
		}
		return serverValue
	}

	protected DslProperty serverCookieValue(Object value, YamlContract.TestCookieMatcher matcher, String key) {
		Object serverValue = value
		if (matcher?.regex) {
			serverValue = Pattern.compile(matcher.regex)
			Pattern pattern = (Pattern) serverValue
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			serverValue = pattern
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.command) {
			return new DslProperty(new ExecutionProperty(matcher.command), value)
		}
		return new DslProperty(value, serverValue)
	}

	protected DslProperty clientValue(Object value, YamlContract.KeyValueMatcher matcher, String key) {
		Object clientValue = value instanceof DslProperty ? value.clientValue : value
		if (matcher?.regex) {
			clientValue = Pattern.compile(matcher.regex)
			Pattern pattern = (Pattern) clientValue
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			clientValue = pattern
			assertPatternMatched(pattern, value, key)
		}
		else if (matcher?.command) {
			return new DslProperty(value, new ExecutionProperty(matcher.command))
		}
		return new DslProperty(clientValue, value)
	}

	protected Object queryParamValue(YamlContract yamlContract, String key, Object value) {
		Request request = new Request()
		YamlContract.QueryParameterMatcher matcher = yamlContract.request.
			matchers.queryParameters.find { it.key == key }
		if (!matcher) {
			return value
		}
		switch (matcher.type) {
		case YamlContract.MatchingType.equal_to:
			return new DslProperty(request.equalTo(matcher.value), value)
		case YamlContract.MatchingType.containing:
			return new DslProperty(request.containing(matcher.value), value)
		case YamlContract.MatchingType.matching:
			return new DslProperty(request.matching(matcher.value), value)
		case YamlContract.MatchingType.not_matching:
			return new DslProperty(request.notMatching(matcher.value), value)
		case YamlContract.MatchingType.equal_to_json:
			return new DslProperty(request.equalToJson(matcher.value), value)
		case YamlContract.MatchingType.equal_to_xml:
			return new DslProperty(request.equalToXml(matcher.value), value)
		case YamlContract.MatchingType.absent:
			return new DslProperty(request.absent(), null)
		default:
			throw new UnsupportedOperationException("The provided matching type [" + matcher + "] is unsupported. Use on of "
				+ YamlContract.MatchingType.
				values())
		}
	}

	protected Object serverValue(Object value, YamlContract.KeyValueMatcher matcher) {
		Object serverValue = value
		if (matcher?.command) {
			return new ExecutionProperty(matcher.command)
		}
		return serverValue instanceof DslProperty ?
			((DslProperty) serverValue).serverValue : serverValue
	}

	private void assertPatternMatched(Pattern pattern, value, String key) {
		boolean matches = pattern.matcher(value.toString()).matches()
		if (!matches) {
			throw new IllegalStateException("Broken headers! A header with "
				+
				"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
		}
	}

	protected Pattern predefinedToPattern(YamlContract.PredefinedRegex predefinedRegex) {
		RegexPatterns patterns = new RegexPatterns()
		switch (predefinedRegex) {
		case YamlContract.PredefinedRegex.only_alpha_unicode:
			return patterns.onlyAlphaUnicode().pattern
		case YamlContract.PredefinedRegex.number:
			return patterns.number().pattern
		case YamlContract.PredefinedRegex.any_double:
			return patterns.aDouble().pattern
		case YamlContract.PredefinedRegex.any_boolean:
			return patterns.anyBoolean().pattern
		case YamlContract.PredefinedRegex.ip_address:
			return patterns.ipAddress().pattern
		case YamlContract.PredefinedRegex.hostname:
			return patterns.hostname().pattern
		case YamlContract.PredefinedRegex.email:
			return patterns.email().pattern
		case YamlContract.PredefinedRegex.url:
			return patterns.url().pattern
		case YamlContract.PredefinedRegex.uuid:
			return patterns.uuid().pattern
		case YamlContract.PredefinedRegex.iso_date:
			return patterns.isoDate().pattern
		case YamlContract.PredefinedRegex.iso_date_time:
			return patterns.isoDateTime().pattern
		case YamlContract.PredefinedRegex.iso_time:
			return patterns.isoTime().pattern
		case YamlContract.PredefinedRegex.iso_8601_with_offset:
			return patterns.iso8601WithOffset().pattern
		case YamlContract.PredefinedRegex.non_empty:
			return patterns.nonEmpty().pattern
		case YamlContract.PredefinedRegex.non_blank:
			return patterns.nonBlank().pattern
		default:
			throw new UnsupportedOperationException("The predefined regex [" + predefinedRegex + "] is unsupported. Use on of "
				+ YamlContract.PredefinedRegex.
				values())
		}
	}

	protected String file(String relativePath) {
		URL resource = Thread.currentThread().getContextClassLoader().
			getResource(relativePath)
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
}
