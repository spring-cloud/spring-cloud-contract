package org.springframework.cloud.contract.verifier.converter

import java.util.regex.Pattern

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.BodyMatcher
import org.springframework.cloud.contract.spec.internal.Cookies
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Headers
import org.springframework.cloud.contract.spec.internal.MatchingType
import org.springframework.cloud.contract.spec.internal.Multipart
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.verifier.util.JsonPaths
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
class ContractsToYaml {

	List<YamlContract> convertTo(Collection<Contract> contracts) {
		return contracts.collect { Contract contract ->
			YamlContract yamlContract = new YamlContract()
			if (contract == null) {
				return yamlContract
			}
			yamlContract.name = contract.name
			yamlContract.ignored = contract.ignored
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
		yamlContract.outputMessage = new YamlContract.OutputMessage()
		yamlContract.outputMessage.sentTo = MapConverter.getStubSideValues(contract.outputMessage.sentTo)
		yamlContract.outputMessage.headers = (contract.outputMessage?.headers as Headers)?.asStubSideMap()
		yamlContract.outputMessage.body = MapConverter.getStubSideValues(contract.outputMessage?.body)
		contract.outputMessage?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
			yamlContract.outputMessage.matchers.body << new YamlContract.BodyTestMatcher(
					path: matcher.path(),
					type: testMatcherType(matcher.matchingType()),
					value: matcher.value()?.toString(),
					minOccurrence: matcher.minTypeOccurrence(),
					maxOccurrence: matcher.maxTypeOccurrence()
			)
		}
		setOutputBodyMatchers(contract.outputMessage?.body, yamlContract.outputMessage.matchers.body)
		setOutputHeadersMatchers(contract.outputMessage?.headers, yamlContract.outputMessage.matchers.headers)
	}

	protected void input(Contract contract, YamlContract yamlContract) {
		if (!contract.input) {
			return
		}
		yamlContract.input = new YamlContract.Input()
		yamlContract.input.assertThat = MapConverter.getTestSideValues(contract.input?.assertThat?.toString())
		yamlContract.input.triggeredBy = MapConverter.getTestSideValues(contract.input?.triggeredBy?.toString())
		yamlContract.input.messageHeaders = (contract.input?.messageHeaders as Headers)?.asTestSideMap()
		yamlContract.input.messageBody = MapConverter.getTestSideValues(contract.input?.messageBody)
		yamlContract.input.messageFrom = MapConverter.getTestSideValues(contract.input?.messageFrom)
		contract.input?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
			yamlContract.input.matchers.body << new YamlContract.BodyStubMatcher(
					path: matcher.path(),
					type: stubMatcherType(matcher.matchingType()),
					value: matcher.value()?.toString()
			)
		}
		setInputBodyMatchers(contract.input?.messageBody, yamlContract.input.matchers.body)
		setInputHeadersMatchers(contract.input?.messageHeaders as Headers, yamlContract.input.matchers.headers)
	}

	protected void request(Contract contract, YamlContract yamlContract) {
		if (!contract.request) {
			return
		}
		yamlContract.request = new YamlContract.Request()
		yamlContract.request.with { YamlContract.Request request ->
			request.method = contract.request?.method?.serverValue
			request.url = contract.request?.url?.serverValue
			request.urlPath = contract.request?.urlPath?.serverValue
			request.headers = (contract.request?.headers as Headers)?.asMap {
				String headerName, DslProperty prop ->
					MapConverter.getTestSideValues(prop).toString()
			}
			request.cookies = (contract.request?.cookies as Cookies)?.asTestSideMap()
			request.body = MapConverter.getTestSideValues(contract.request?.body)
			Multipart multipart = contract.request.multipart
			if (multipart) {
				request.multipart = new YamlContract.Multipart()
				Map<String, Object> map = (Map<String, Object>) MapConverter.getTestSideValues(multipart)
				map.each { String key, Object value ->
					if (value instanceof NamedProperty) {
						Object fileName = value.name?.serverValue
						Object contentType = value.contentType?.serverValue
						Object fileContent = value.value?.serverValue
						request.multipart.named << new YamlContract.Named(paramName: key,
								fileName: fileName instanceof String ? value.name?.serverValue as String : null,
								fileContent: fileContent instanceof String ? fileContent as String : null,
								fileContentAsBytes: fileContent instanceof String ? fileContent as String : null,
								contentType: contentType instanceof String ? contentType as String : null,
								fileNameCommand: fileName instanceof ExecutionProperty ? fileName.toString() : null,
								fileContentCommand: fileContent instanceof ExecutionProperty ? fileContent.toString() : null,
								contentTypeCommand: contentType instanceof ExecutionProperty ? contentType.toString() : null)
					} else {
						request.multipart.params.put(key, value != null ? value.toString() : null)
					}
				}
			}
			request.matchers = new YamlContract.StubMatchers()
			contract.request?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
				request.matchers.body << new YamlContract.BodyStubMatcher(
						path: matcher.path(),
						type: stubMatcherType(matcher.matchingType()),
						value: matcher.value()?.toString()
				)
			}
			Object url = contract.request.url?.clientValue
			request.matchers.url =  url instanceof Pattern ?
					new YamlContract.KeyValueMatcher(regex: url.pattern()) :
					url instanceof ExecutionProperty ?
							new YamlContract.KeyValueMatcher(command: url.toString()) : null
			Object urlPath = contract.request.urlPath?.clientValue
			request.matchers.url =  urlPath instanceof Pattern ?
					new YamlContract.KeyValueMatcher(regex: urlPath.pattern()) :
					urlPath instanceof ExecutionProperty ?
							new YamlContract.KeyValueMatcher(command: urlPath.toString()) : null
			if (multipart) {
				request.matchers.multipart = new YamlContract.MultipartStubMatcher()
				Map<String, Object> map = (Map<String, Object>) MapConverter.getStubSideValues(multipart)
				map.each { String key, Object value ->
					if (value instanceof NamedProperty) {
						Object fileName = value.name?.clientValue
						Object fileContent = value.value?.clientValue
						Object contentType = value.contentType?.clientValue
						if (fileName instanceof Pattern ||
								fileContent instanceof Pattern ||
								contentType instanceof Pattern) {
							request.matchers.multipart.named << new YamlContract.MultipartNamedStubMatcher(
									paramName: key,
									fileName: valueMatcher(fileName),
									fileContent: valueMatcher(fileContent),
									contentType: valueMatcher(contentType),
							)
						}
					} else if (value instanceof Pattern) {
						request.matchers.multipart.params.add(new YamlContract.KeyValueMatcher(
								key: key,
								regex: value.pattern()
						))
					}
				}
			}
			// TODO: Cookie matchers - including absent
			setInputBodyMatchers(contract.request?.body, request.matchers.body)
			setInputHeadersMatchers(contract.request?.headers as Headers, yamlContract.request.matchers.headers)
		}
	}

	protected YamlContract.ValueMatcher valueMatcher(Object o) {
		return o instanceof Pattern ? new YamlContract.ValueMatcher(regex: o.pattern()) : null
	}

	protected void setInputBodyMatchers(DslProperty body, List<YamlContract.BodyStubMatcher> bodyMatchers) {
		JsonPaths paths = new JsonToJsonPathsConverter().transformToJsonPathWithStubsSideValues(body)
		paths?.findAll { it.valueBeforeChecking() instanceof Pattern }?.each {
			bodyMatchers << new YamlContract.BodyStubMatcher(
					path: it.keyBeforeChecking(),
					type: YamlContract.StubMatcherType.by_regex,
					value: (it.valueBeforeChecking() as Pattern).pattern()
			)
		}
	}

	protected void setOutputBodyMatchers(DslProperty body, List<YamlContract.BodyTestMatcher> bodyMatchers) {
		JsonPaths paths = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(body)
		paths?.findAll { it.valueBeforeChecking() instanceof Pattern }?.each {
			bodyMatchers << new YamlContract.BodyTestMatcher(
					path: it.keyBeforeChecking(),
					type: YamlContract.TestMatcherType.by_regex,
					value: (it.valueBeforeChecking() as Pattern).pattern()
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
			response.body = MapConverter.getStubSideValues(contract.response?.body)
			contract.response?.bodyMatchers?.jsonPathMatchers()?.each { BodyMatcher matcher ->
				response.matchers.body << new YamlContract.BodyTestMatcher(
						path: matcher.path(),
						type: testMatcherType(matcher.matchingType()),
						value: matcher.value()?.toString(),
						minOccurrence: matcher.minTypeOccurrence(),
						maxOccurrence: matcher.maxTypeOccurrence()
				)
			}
			setOutputBodyMatchers(contract.response?.body, yamlContract.response.matchers.body)
			setOutputHeadersMatchers(contract.response?.headers, yamlContract.response.matchers.headers)
		}
	}

	protected void setInputHeadersMatchers(Headers headers, List<YamlContract.KeyValueMatcher> headerMatchers) {
		headers?.asStubSideMap()?.each { String key, Object value ->
			if (value instanceof Pattern) {
				headerMatchers << new YamlContract.KeyValueMatcher(
						key: key,
						regex: value.pattern(),
				)
			}
		}
	}

	protected void setOutputHeadersMatchers(Headers headers, List<YamlContract.TestHeaderMatcher> headerMatchers) {
		headers?.asTestSideMap()?.each { String key, Object value ->
			if (value instanceof Pattern) {
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						regex: value.pattern(),
				)
			} else if (value instanceof ExecutionProperty) {
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						command: value.executionCommand,
				)
			} else if (value instanceof NotToEscapePattern) {
				headerMatchers << new YamlContract.TestHeaderMatcher(
						key: key,
						regex: value.serverValue.pattern(),
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
