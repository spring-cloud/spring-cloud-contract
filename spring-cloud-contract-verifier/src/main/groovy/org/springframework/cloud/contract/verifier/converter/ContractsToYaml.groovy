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

import java.util.regex.Pattern

import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.Cookies
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.Multipart
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.spec.internal.RegexProperty
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.converter.YamlContract.RegexType
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.verifier.util.MapConverter

import static org.springframework.cloud.contract.verifier.util.ContentType.XML
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateClientSideContentType

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 */
@PackageScope
class ContractsToYaml {

	List<YamlContract> convertTo(Collection<Contract> contracts) {
		return contracts.collect { Contract contract ->
			YamlContract yamlContract = new YamlContract()
			if (contract == null) {
				return yamlContract
			}
			yamlContract.name = contract.name
			yamlContract.ignored = contract.ignored
			yamlContract.inProgress = contract.inProgress
			yamlContract.description = contract.description
			yamlContract.label = contract.label
			request(contract, yamlContract)
			response(yamlContract, contract)
			input(contract, yamlContract)
			output(contract, yamlContract)
			return yamlContract
		}
	}

	protected void output(Contract contract, YamlContract yamlContract) {
		if (!contract.outputMessage) {
			return
		}
		ContentType contentType = evaluateClientSideContentType(contract.response?.headers,
				contract.response?.body)
		yamlContract.outputMessage = new YamlContract.OutputMessage()
		yamlContract.outputMessage.sentTo = MapConverter.
				getStubSideValues(contract.outputMessage.sentTo)
		yamlContract.outputMessage.headers = (contract.outputMessage?.headers as Headers)?.
				asStubSideMap()
		yamlContract.outputMessage.body = MapConverter.getStubSideValues(
				contract.outputMessage?.body)
		contract.outputMessage?.bodyMatchers?.matchers()?.each { BodyMatcher matcher ->
			yamlContract.outputMessage.matchers.body << new YamlContract.BodyTestMatcher(
					path: matcher.path(),
					type: testMatcherType(matcher.matchingType()),
					value: matcher.value()?.toString(),
					minOccurrence: matcher.minTypeOccurrence(),
					maxOccurrence: matcher.maxTypeOccurrence()
			)
		}
		if (XML != contentType) {
			setOutputBodyMatchers(contract.outputMessage?.body,
					yamlContract.outputMessage.matchers.body)
		}
		setOutputHeadersMatchers(contract.outputMessage?.headers,
				yamlContract.outputMessage.matchers.headers)
	}

	protected void input(Contract contract, YamlContract yamlContract) {
		if (!contract.input) {
			return
		}
		ContentType contentType = evaluateClientSideContentType(contract.input?.messageHeaders,
				contract.input?.messageBody)
		yamlContract.input = new YamlContract.Input()
		yamlContract.input.assertThat = MapConverter.
				getTestSideValues(contract.input?.assertThat?.toString())
		yamlContract.input.triggeredBy = MapConverter.
				getTestSideValues(contract.input?.triggeredBy?.toString())
		yamlContract.input.messageHeaders = (contract.input?.messageHeaders as Headers)?.
				asTestSideMap()
		yamlContract.input.messageBody = MapConverter.
				getTestSideValues(contract.input?.messageBody)
		yamlContract.input.messageFrom = MapConverter.
				getTestSideValues(contract.input?.messageFrom)
		contract.input?.bodyMatchers?.matchers()?.each { BodyMatcher matcher ->
			yamlContract.input.matchers.body << new YamlContract.BodyStubMatcher(
					path: matcher.path(),
					type: stubMatcherType(matcher.matchingType()),
					value: matcher.value()?.toString()
			)
		}
		if (XML != contentType) {
			setInputBodyMatchers(contract.input?.messageBody, yamlContract.input.matchers.body)
		}
		setInputHeadersMatchers(contract.input?.messageHeaders as Headers, yamlContract.input.matchers.headers)
	}

	protected void request(Contract contract, YamlContract yamlContract) {
		if (!contract.request) {
			return
		}
		ContentType requestContentType = evaluateClientSideContentType(contract.request.headers,
				contract.request.body)
		yamlContract.request = new YamlContract.Request()
		yamlContract.request.with { YamlContract.Request request ->
			request.method = contract.request?.method?.serverValue
			request.url = contract.request?.url?.serverValue
			request.urlPath = contract.request?.urlPath?.serverValue
			request.matchers = new YamlContract.StubMatchers()
			Url requestUrl = contract.request.url ?: contract.request.urlPath
			if (requestUrl.queryParameters != null) {
				request.queryParameters = requestUrl.queryParameters
						.parameters.collectEntries {
					def testSide = MapConverter.getTestSideValuesForNonBody(it)
					def stubSide = it.clientValue
					if (stubSide instanceof RegexProperty || stubSide instanceof Pattern) {
						request.matchers.queryParameters.add(new YamlContract.QueryParameterMatcher(key: it.name, type: YamlContract.MatchingType.matching, value: new RegexProperty(stubSide).pattern()))
					}
					else if (stubSide instanceof MatchingStrategy) {
						request.matchers.queryParameters.add(new YamlContract.QueryParameterMatcher(key: it.name, type: YamlContract.MatchingType.from(stubSide.getType().name), value: MapConverter.getStubSideValuesForNonBody(stubSide)))
					}
					return [(it.name): testSide]
						}
			}
			request.headers = (contract.request?.headers as Headers)?.asMap {
				String headerName, DslProperty prop ->
					def testSideValue = MapConverter.getTestSideValues(prop)
					if (testSideValue instanceof ExecutionProperty) {
						return MapConverter.getStubSideValuesForNonBody(prop).toString()
					}
					return testSideValue.toString()
			}
			request.cookies = (contract.request?.cookies as Cookies)?.asTestSideMap()
			Object body = contract.request?.body?.serverValue
			if (body instanceof FromFileProperty) {
				if (body.isByte()) {
					request.bodyFromFileAsBytes = body.fileName()
				}
				if (body.isString()) {
					request.bodyFromFile = body.fileName()
				}
			}
			else {
				request.body = MapConverter.getTestSideValues(contract.request?.body)
			}
			Multipart multipart = contract.request.multipart
			if (multipart) {
				request.multipart = new YamlContract.Multipart()
				Map<String, Object> map = (Map<String, Object>) MapConverter.
						getTestSideValues(multipart)
				map.each { String key, Object value ->
					if (value instanceof NamedProperty) {
						Object fileName = value.name?.serverValue
						Object contentType = value.contentType?.serverValue
						Object fileContent = value.value?.serverValue
						request.multipart.named << new YamlContract.Named(paramName: key,
								fileName: fileName instanceof String ? value.name?.serverValue as String : null,
								fileContent: fileContent instanceof String ? fileContent as String : null,
								fileContentAsBytes: fileContent instanceof FromFileProperty ? fileContent.asBytes().toString() : null,
								fileContentFromFileAsBytes: resolveFileNameAsBytes(fileContent),
								contentType: contentType instanceof String ? contentType as String : null,
								fileNameCommand: fileName instanceof ExecutionProperty ? fileName.toString() : null,
								fileContentCommand: fileContent instanceof ExecutionProperty ? fileContent.toString() : null,
								contentTypeCommand: contentType instanceof ExecutionProperty ? contentType.toString() : null)
					}
					else {
						request.multipart.params.put(key, value != null ? value.toString() : null)
					}
				}
			}
			contract.request?.bodyMatchers?.matchers()?.each { BodyMatcher matcher ->
				request.matchers.body << new YamlContract.BodyStubMatcher(
						path: matcher.path(),
						type: stubMatcherType(matcher.matchingType()),
						value: matcher.value()?.toString(),
						minOccurrence: matcher.minTypeOccurrence(),
						maxOccurrence: matcher.maxTypeOccurrence(),
				)
			}
			Object url = contract.request.url?.clientValue
			request.matchers.url = url instanceof RegexProperty ?
					new YamlContract.KeyValueMatcher(regex: url.pattern()) :
					url instanceof ExecutionProperty ?
							new YamlContract.KeyValueMatcher(command: url.toString()) : null
			Object urlPath = contract.request.urlPath?.clientValue
			request.matchers.url = urlPath instanceof RegexProperty ?
					new YamlContract.KeyValueMatcher(regex: urlPath.pattern()) :
					urlPath instanceof ExecutionProperty ?
							new YamlContract.KeyValueMatcher(command: urlPath.toString()) : null
			if (multipart) {
				request.matchers.multipart = new YamlContract.MultipartStubMatcher()
				Map<String, Object> map = (Map<String, Object>) MapConverter.
						getStubSideValues(multipart)
				map.each { String key, Object value ->
					if (value instanceof NamedProperty) {
						Object fileName = value.name?.clientValue
						Object fileContent = value.value?.clientValue
						Object contentType = value.contentType?.clientValue
						if (fileName instanceof RegexProperty ||
								fileContent instanceof RegexProperty ||
								contentType instanceof RegexProperty) {
							request.matchers.multipart.named << new YamlContract.MultipartNamedStubMatcher(
									paramName: key,
									fileName: valueMatcher(fileName),
									fileContent: valueMatcher(fileContent),
									contentType: valueMatcher(contentType),
							)
						}
					}
					else if (value instanceof RegexProperty || value instanceof Pattern) {
						RegexProperty property = new RegexProperty(value)
						request.matchers.multipart.params.
								add(new YamlContract.KeyValueMatcher(
										key: key,
										regex: property.pattern(),
										regexType: regexType(property.clazz())
								))
					}
				}
			}
			// TODO: Cookie matchers - including absent
			if (XML != requestContentType) {
				setInputBodyMatchers(contract.request?.body, request.matchers.body)
			}
			setInputHeadersMatchers(contract.request?.headers as Headers, yamlContract.request.matchers.headers)
		}
	}

	protected String resolveFileNameAsBytes(Object value) {
		if (!(value instanceof FromFileProperty)) {
			return null
		}
		FromFileProperty property = (FromFileProperty) value
		return property.fileName()
	}

	protected YamlContract.ValueMatcher valueMatcher(Object o) {
		return o instanceof RegexProperty ? new YamlContract.ValueMatcher(regex: o.
				pattern()) : null
	}

	protected void setInputBodyMatchers(DslProperty body, List<YamlContract.BodyStubMatcher> bodyMatchers) {
		def testSideValues = MapConverter.getTestSideValues(body)
		JsonPaths paths = new JsonToJsonPathsConverter().
				transformToJsonPathWithStubsSideValues(body)
		paths?.findAll { it.valueBeforeChecking() instanceof Pattern }?.each {
			Object element = JsonToJsonPathsConverter.readElement(testSideValues, it.keyBeforeChecking())
			bodyMatchers << new YamlContract.BodyStubMatcher(
					path: it.keyBeforeChecking(),
					type: YamlContract.StubMatcherType.by_regex,
					value: (it.valueBeforeChecking() as Pattern).pattern(),
					regexType: regexType(element)
			)
		}
	}

	protected RegexType regexType(Object from) {
		return regexType(from.class)
	}

	protected RegexType regexType(Class clazz) {
		switch (clazz) {
		case Boolean:
			return RegexType.as_boolean
		case Long:
			return RegexType.as_long
		case Short:
			return RegexType.as_short
		case Integer:
			return RegexType.as_integer
		case Float:
			return RegexType.as_float
		case Double:
			return RegexType.as_double
		default:
			return RegexType.as_string
		}
	}

	protected void setOutputBodyMatchers(DslProperty body,
			List<YamlContract.BodyTestMatcher> bodyMatchers) {
		def testSideValues = MapConverter.getTestSideValues(body)
		JsonPaths paths = new JsonToJsonPathsConverter().
				transformToJsonPathWithTestsSideValues(body)
		paths?.findAll { it.valueBeforeChecking() instanceof Pattern }?.each {
			Object element = JsonToJsonPathsConverter.readElement(testSideValues, it.keyBeforeChecking())
			bodyMatchers << new YamlContract.BodyTestMatcher(
					path: it.keyBeforeChecking(),
					type: YamlContract.TestMatcherType.by_regex,
					value: (it.valueBeforeChecking() as Pattern).pattern(),
					regexType: regexType(element)
			)
		}
		if (body?.serverValue instanceof Pattern) {
			bodyMatchers << new YamlContract.BodyTestMatcher(
					type: YamlContract.TestMatcherType.by_regex,
					value: ((Pattern) body.serverValue).pattern()
			)
		}
	}

	protected void response(YamlContract yamlContract, Contract contract) {
		if (!contract.response) {
			return
		}
		ContentType contentType = evaluateClientSideContentType(contract.response?.headers,
				contract.response?.body)
		yamlContract.response = new YamlContract.Response()
		yamlContract.response.with { YamlContract.Response response ->
			response.async = contract.response.async
			response.fixedDelayMilliseconds = contract.response?.delay?.clientValue as Integer
			response.status = contract.response?.status?.clientValue as Integer
			response.headers = (contract.response?.headers as Headers)?.asMap {
				String headerName, DslProperty prop ->
					MapConverter.getStubSideValues(prop).toString()
			}
			response.cookies = (contract.response?.cookies as Cookies)?.asStubSideMap()
			Object body = contract.response?.body?.clientValue
			if (body instanceof FromFileProperty) {
				if (body.isByte()) {
					response.bodyFromFileAsBytes = body.fileName()
				}
				if (body.isString()) {
					response.bodyFromFile = body.fileName()
				}
			}
			else {
				response.body = MapConverter.getStubSideValues(contract.response?.body)
			}
			contract.response?.bodyMatchers?.matchers()?.each { BodyMatcher matcher ->
				response.matchers.body << new YamlContract.BodyTestMatcher(
						path: matcher.path(),
						type: testMatcherType(matcher.matchingType()),
						value: matcher.value()?.toString(),
						minOccurrence: matcher.minTypeOccurrence(),
						maxOccurrence: matcher.maxTypeOccurrence()
				)
			}
			if (XML != contentType) {
				setOutputBodyMatchers(contract.response?.body,
						yamlContract.response.matchers.body)
			}
			setOutputHeadersMatchers(contract.response?.headers,
					yamlContract.response.matchers.headers)
		}
	}

	protected void setInputHeadersMatchers(Headers headers, List<YamlContract.KeyValueMatcher> headerMatchers) {
		headers?.asStubSideMap()?.each { String key, Object value ->
			if (value instanceof RegexProperty || value instanceof Pattern) {
				RegexProperty property = new RegexProperty(value)
				headerMatchers << new YamlContract.KeyValueMatcher(
						key: key,
						regex: property.pattern(),
						regexType: regexType(property.clazz())
				)
			}
		}
	}

	protected void setOutputHeadersMatchers(Headers headers, List<YamlContract.TestHeaderMatcher> headerMatchers) {
		headers?.asTestSideMap()?.each { String key, Object value ->
			if (value instanceof RegexProperty || value instanceof Pattern) {
				RegexProperty property = new RegexProperty(value)
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						regex: property.pattern(),
						regexType: regexType(property.clazz())
				)
			}
			else if (value instanceof ExecutionProperty) {
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						command: value.executionCommand,
				)
			}
			else if (value instanceof NotToEscapePattern) {
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						regex: ((Pattern) value.serverValue).pattern(),
				)
			}
		}
	}

	protected YamlContract.TestMatcherType testMatcherType(MatchingType matchingType) {
		switch (matchingType) {
		case MatchingType.EQUALITY:
			return YamlContract.TestMatcherType.by_equality
		case MatchingType.TYPE:
			return YamlContract.TestMatcherType.by_type
		case MatchingType.COMMAND:
			return YamlContract.TestMatcherType.by_command
		case MatchingType.DATE:
			return YamlContract.TestMatcherType.by_date
		case MatchingType.TIME:
			return YamlContract.TestMatcherType.by_time
		case MatchingType.TIMESTAMP:
			return YamlContract.TestMatcherType.by_timestamp
		case MatchingType.REGEX:
			return YamlContract.TestMatcherType.by_regex
		case MatchingType.NULL:
			return YamlContract.TestMatcherType.by_null
		}
		return null
	}

	protected YamlContract.StubMatcherType stubMatcherType(MatchingType matchingType) {
		switch (matchingType) {
		case MatchingType.EQUALITY:
			return YamlContract.StubMatcherType.by_equality
		case MatchingType.TYPE:
		case MatchingType.COMMAND:
			throw new UnsupportedOperationException("No type for client side")
		case MatchingType.DATE:
			return YamlContract.StubMatcherType.by_date
		case MatchingType.TIME:
			return YamlContract.StubMatcherType.by_time
		case MatchingType.TIMESTAMP:
			return YamlContract.StubMatcherType.by_timestamp
		case MatchingType.REGEX:
			return YamlContract.StubMatcherType.by_regex
		}
		return null
	}
}
