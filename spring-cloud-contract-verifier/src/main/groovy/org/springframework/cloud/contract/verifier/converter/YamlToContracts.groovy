package org.springframework.cloud.contract.verifier.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.MatchingTypeValue
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.RegexPatterns
import org.springframework.cloud.contract.spec.internal.Request
import org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.util.regex.Pattern
/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
@PackageScope
class YamlToContracts {

	Collection<Contract> convertFrom(File contractFile) {
		ClassLoader classLoader = YamlContractConverter.getClassLoader()
		YAMLMapper mapper = new YAMLMapper()
		try {
			Iterable<Object> iterables = new Yaml().loadAll(Files.newInputStream(contractFile.toPath()))
			Collection<Contract> contracts = []
			for (Object o : iterables) {
				Closure<List<Contract>> processYaml = processYaml(mapper, classLoader, contractFile)
				contracts.addAll(processYaml(o))
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
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader)
		}
	}

	protected Closure<List<Contract>> processYaml(ObjectMapper mapper, ClassLoader classLoader, File contractFile) {
		return {
			List<YamlContract> yamlContracts = convert(mapper, it)
			Thread.currentThread().setContextClassLoader(updatedClassLoader(contractFile.getParentFile(), classLoader))
			return yamlContracts.collect { YamlContract yamlContract ->
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
								url(urlValue(yamlContract.request?.url, yamlContract.request?.matchers?.url)) {
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
								urlPath(urlValue(yamlContract.request?.urlPath, yamlContract.request?.matchers?.url)) {
									if (yamlContract.request.queryParameters) {
										queryParameters {
											yamlContract.request.queryParameters.each { String key, Object value ->
												if (value instanceof List) {
													((List) value).each {
														parameter(key, queryParamValue(yamlContract, key, it))
													}
												} else {
													parameter(key, queryParamValue(yamlContract, key, value))
												}
											}
										}
									}
								}
							}
							if (yamlContract.request?.headers) {
								headers {
									yamlContract.request.headers.each { String key, Object value ->
										List<YamlContract.KeyValueMatcher> matchers =
												yamlContract.request.matchers.headers.findAll { it.key == key }
										matchers.each { YamlContract.KeyValueMatcher matcher ->
											if (value instanceof List) {
												((List) value).each {
													header(key, clientValue(it, matcher, key).clientValue)
												}
											} else {
												header(key, new DslProperty(clientValue(value, matcher, key).clientValue,
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
									yamlContract.request?.cookies?.each { String key, Object value ->
										YamlContract.KeyValueMatcher matcher = yamlContract.request.matchers.cookies.find { it.key == key }
										cookie(key, clientValue(value, matcher, key))
									}
								}
							}
							if (yamlContract.request.body != null) body(yamlContract.request.body)
							if (yamlContract.request.bodyFromFile != null) body(file(yamlContract.request.bodyFromFile))
							if (yamlContract.request.multipart) {
								Map multipartMap = [:]
								Map<String, DslProperty> multiPartParams = yamlContract.request
										.multipart.params.collectEntries { String paramKey, String paramValue ->
									YamlContract.KeyValueMatcher matcher = yamlContract.request.matchers
											.multipart.params.find {
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
								yamlContract.request.multipart.named.each { YamlContract.Named namedParam ->
									YamlContract.MultipartNamedStubMatcher matcher = yamlContract.request.matchers.multipart.named.find {
										it.paramName == namedParam.paramName
									}
									Object fileNameValue = namedParam.fileName
									Object fileContentValue = namedParam.fileContent
									String fileContentValueAsBytes = namedParam.fileContentAsBytes
									String contentTypeCommand = namedParam.contentTypeCommand
									String fileContentCommand = namedParam.fileContentCommand
									String fileNameCommand = namedParam.fileNameCommand
									Object contentTypeValue = namedParam.contentType
									if (matcher && matcher.fileName) {
										fileNameValue = matcher.fileName.regex ? Pattern.compile(matcher.fileName.regex) :
												predefinedToPattern(matcher.fileName.predefined)
									}
									if (matcher && matcher.fileContent) {
										fileContentValue = matcher.fileContent.regex ? Pattern.compile(matcher.fileContent.regex) :
												predefinedToPattern(matcher.fileContent.predefined)
									}
									if (matcher && matcher.contentType) {
										contentTypeValue = matcher.contentType.regex ? Pattern.compile(matcher.contentType.regex) :
												predefinedToPattern(matcher.contentType.predefined)
									}
									multipartMap.put(namedParam.paramName, new NamedProperty(
											new DslProperty<>(fileNameValue, fileNameCommand ? new ExecutionProperty(fileNameCommand)
													: namedParam.fileName),
											new DslProperty<>(fileContentValue, namedParam.fileContent ?: fileContentValueAsBytes
													?: new ExecutionProperty(fileContentCommand)),
											new DslProperty(contentTypeValue, contentTypeCommand ? new ExecutionProperty(contentTypeCommand)
													: namedParam.contentType)))
								}
								multipart(multipartMap)
							}
							bodyMatchers {
								yamlContract.request.matchers?.body?.each { YamlContract.BodyStubMatcher matcher ->
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
												regex = predefinedToPattern(matcher.predefined).pattern()
											}
											value = byRegex(regex)
											break
										case YamlContract.StubMatcherType.by_equality:
											value = byEquality()
											break
										case YamlContract.StubMatcherType.by_null:
											// do nothing
											break
										default:
											throw new UnsupportedOperationException("The type [" + matcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
									}
									if (value) {
										jsonPath(matcher.path, value)
									}
								}
							}
						}
						response {
							status(yamlContract.response.status)
							headers {
								yamlContract.response?.headers?.each { String key, Object value ->
									YamlContract.TestHeaderMatcher matcher = yamlContract.response.matchers.headers.find { it.key == key }
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
							if (yamlContract.response?.cookies) {
								cookies {
									yamlContract.response?.cookies?.each { String key, Object value ->
										YamlContract.TestCookieMatcher matcher = yamlContract.response.matchers.cookies.find { it.key == key }
										DslProperty cookieValue = serverCookieValue(value, matcher, key)
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
											Pattern.compile(bodyTestMatcher.value) : new ExecutionProperty(bodyTestMatcher.value)))
								} else {
									body(yamlContract.response.body)
								}
							}
							if (yamlContract.response.bodyFromFile) body(file(yamlContract.response.bodyFromFile))
							if (yamlContract.response.async) async()
							if (yamlContract.response.fixedDelayMilliseconds) fixedDelayMilliseconds(yamlContract.response.fixedDelayMilliseconds)
							bodyMatchers {
								yamlContract.response?.matchers?.body?.each { YamlContract.BodyTestMatcher testMatcher ->
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
												regex = predefinedToPattern(testMatcher.predefined).pattern()
											}
											value = byRegex(regex)
											break
										case YamlContract.TestMatcherType.by_equality:
											value = byEquality()
											break
										case YamlContract.TestMatcherType.by_type:
											value = byType() {
												if (testMatcher.minOccurrence != null) minOccurrence(testMatcher.minOccurrence)
												if (testMatcher.maxOccurrence != null) maxOccurrence(testMatcher.maxOccurrence)
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
										jsonPath(testMatcher.path, value)
									}
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
									YamlContract.KeyValueMatcher matcher = yamlContract.input.matchers?.headers?.find { it.key == key }
									header(key, clientValue(value, matcher, key))
								}
							}
							if (yamlContract.input.messageBody) messageBody(yamlContract.input.messageBody)
							if (yamlContract.input.messageBodyFromFile) messageBody(file(yamlContract.input.messageBodyFromFile))
							bodyMatchers {
								yamlContract.input.matchers.body?.each { YamlContract.BodyStubMatcher matcher ->
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
												regex = predefinedToPattern(matcher.predefined).pattern()
											}
											value = byRegex(regex)
											break
										case YamlContract.StubMatcherType.by_equality:
											value = byEquality()
											break
										default:
											throw new UnsupportedOperationException("The type [" + matcher.type + "] is unsupported. Hint: If you're using <predefined> remember to pass <type: by_regex>")
									}
									jsonPath(matcher.path, value)
								}
							}
						}
					}
					YamlContract.OutputMessage outputMsg = yamlContract.outputMessage
					if (outputMsg) {
						outputMessage {
							if (outputMsg.assertThat) assertThat(outputMsg.assertThat)
							if (outputMsg.sentTo) sentTo(outputMsg.sentTo)
							headers {
								outputMsg.headers?.each { String key, Object value ->
									YamlContract.TestHeaderMatcher matcher = outputMsg.matchers?.headers?.find { it.key == key }
									Object serverValue = serverValue(value, matcher, key)
									header(key, new DslProperty(value, serverValue))
								}
							}
							if (outputMsg.body) body(outputMsg.body)
							if (outputMsg.bodyFromFile) body(file(outputMsg.bodyFromFile))
							if (outputMsg.matchers) {
								bodyMatchers {
									yamlContract.outputMessage?.matchers?.body?.each { YamlContract.BodyTestMatcher testMatcher ->
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
													regex = predefinedToPattern(testMatcher.predefined).pattern()
												}
												value = byRegex(regex)
												break
											case YamlContract.TestMatcherType.by_equality:
												value = byEquality()
												break
											case YamlContract.TestMatcherType.by_type:
												value = byType() {
													if (testMatcher.minOccurrence != null) minOccurrence(testMatcher.minOccurrence)
													if (testMatcher.maxOccurrence != null) maxOccurrence(testMatcher.maxOccurrence)
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
										jsonPath(testMatcher.path, value)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected DslProperty urlValue(String url, YamlContract.KeyValueMatcher urlMatcher) {
		if (urlMatcher) {
			if (urlMatcher.command) {
				return new DslProperty<Object>(url, new ExecutionProperty(urlMatcher.command))
			}
			return new DslProperty(urlMatcher.regex ? Pattern.compile(urlMatcher.regex) :
					urlMatcher.predefined ? predefinedToPattern(urlMatcher.predefined) : url, url)
		}
		return new DslProperty(url)
	}

	protected List<YamlContract> convert(ObjectMapper mapper, Object o) {
		try {
			return Arrays.asList(mapper.convertValue(o, YamlContract[].class))
		} catch(IllegalArgumentException e) {
			return Collections.singletonList(mapper.convertValue(o, YamlContract.class))
		}
	}

	protected Object serverValue(Object value, YamlContract.TestHeaderMatcher matcher, String key) {
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

	protected DslProperty serverCookieValue(Object value, YamlContract.TestCookieMatcher matcher, String key) {
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
		} else if (matcher?.predefined) {
			Pattern pattern = predefinedToPattern(matcher.predefined)
			clientValue = pattern
			assertPatternMatched(pattern, value, key)
		} else if (matcher?.command) {
			return new DslProperty(value, new ExecutionProperty(matcher.command))
		}
		return new DslProperty(clientValue, value)
	}

	protected Object queryParamValue(YamlContract yamlContract, String key, Object value) {
		Request request = new Request()
		YamlContract.QueryParameterMatcher matcher = yamlContract.request.
				matchers.queryParameters.find { it.key == key}
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
				throw new UnsupportedOperationException("The provided matching type [" + matcher + "] is unsupported. Use on of " + YamlContract.MatchingType.values())
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
		if (!matches) throw new IllegalStateException("Broken headers! A header with " +
				"key [${key}] with value [${value}] is not matched by regex [${pattern.pattern()}]")
	}

	protected Pattern predefinedToPattern(YamlContract.PredefinedRegex predefinedRegex) {
		RegexPatterns patterns = new RegexPatterns()
		switch (predefinedRegex) {
			case YamlContract.PredefinedRegex.only_alpha_unicode:
				return patterns.onlyAlphaUnicode()
			case YamlContract.PredefinedRegex.number:
				return patterns.number()
			case YamlContract.PredefinedRegex.any_double:
				return patterns.aDouble()
			case YamlContract.PredefinedRegex.any_boolean:
				return patterns.anyBoolean()
			case YamlContract.PredefinedRegex.ip_address:
				return patterns.ipAddress()
			case YamlContract.PredefinedRegex.hostname:
				return patterns.hostname()
			case YamlContract.PredefinedRegex.email:
				return patterns.email()
			case YamlContract.PredefinedRegex.url:
				return patterns.url()
			case YamlContract.PredefinedRegex.uuid:
				return patterns.uuid()
			case YamlContract.PredefinedRegex.iso_date:
				return patterns.isoDate()
			case YamlContract.PredefinedRegex.iso_date_time:
				return patterns.isoDateTime()
			case YamlContract.PredefinedRegex.iso_time:
				return patterns.isoTime()
			case YamlContract.PredefinedRegex.iso_8601_with_offset:
				return patterns.iso8601WithOffset()
			case YamlContract.PredefinedRegex.non_empty:
				return patterns.nonEmpty()
			case YamlContract.PredefinedRegex.non_blank:
				return patterns.nonBlank()
			default:
				throw new UnsupportedOperationException("The predefined regex [" + predefinedRegex + "] is unsupported. Use on of " + YamlContract.PredefinedRegex.values())
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
}
