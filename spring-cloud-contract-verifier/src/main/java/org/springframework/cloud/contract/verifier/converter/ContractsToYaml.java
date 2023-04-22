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

package org.springframework.cloud.contract.verifier.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Cookies;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.Multipart;
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.Part;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.springframework.cloud.contract.verifier.util.ContentType.XML;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateClientSideContentType;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Stessy Delcroix
 */
class ContractsToYaml {

	private static final Map<Class<?>, YamlContract.RegexType> PRIMITIVE_WRAPPER_TO_REGEX_TYPE = new HashMap<>();

	private static final Map<MatchingType, YamlContract.TestMatcherType> TEST_MATCHER_TYPE = new HashMap<>();

	private static final Map<MatchingType, YamlContract.StubMatcherType> STUB_MATCHER_TYPE = new HashMap<>();

	static {
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Boolean.class, YamlContract.RegexType.as_boolean);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Long.class, YamlContract.RegexType.as_long);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Short.class, YamlContract.RegexType.as_short);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Integer.class, YamlContract.RegexType.as_integer);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Float.class, YamlContract.RegexType.as_float);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(Double.class, YamlContract.RegexType.as_double);
		PRIMITIVE_WRAPPER_TO_REGEX_TYPE.put(String.class, YamlContract.RegexType.as_string);
		TEST_MATCHER_TYPE.put(MatchingType.EQUALITY, YamlContract.TestMatcherType.by_equality);
		TEST_MATCHER_TYPE.put(MatchingType.TYPE, YamlContract.TestMatcherType.by_type);
		TEST_MATCHER_TYPE.put(MatchingType.COMMAND, YamlContract.TestMatcherType.by_command);
		TEST_MATCHER_TYPE.put(MatchingType.DATE, YamlContract.TestMatcherType.by_date);
		TEST_MATCHER_TYPE.put(MatchingType.TIME, YamlContract.TestMatcherType.by_time);
		TEST_MATCHER_TYPE.put(MatchingType.TIMESTAMP, YamlContract.TestMatcherType.by_timestamp);
		TEST_MATCHER_TYPE.put(MatchingType.REGEX, YamlContract.TestMatcherType.by_regex);
		TEST_MATCHER_TYPE.put(MatchingType.NULL, YamlContract.TestMatcherType.by_null);
		STUB_MATCHER_TYPE.put(MatchingType.EQUALITY, YamlContract.StubMatcherType.by_equality);
		STUB_MATCHER_TYPE.put(MatchingType.DATE, YamlContract.StubMatcherType.by_date);
		STUB_MATCHER_TYPE.put(MatchingType.TIME, YamlContract.StubMatcherType.by_time);
		STUB_MATCHER_TYPE.put(MatchingType.TIMESTAMP, YamlContract.StubMatcherType.by_timestamp);
		STUB_MATCHER_TYPE.put(MatchingType.REGEX, YamlContract.StubMatcherType.by_regex);
	}

	List<YamlContract> convertTo(Collection<Contract> contracts) {

		return contracts.stream().map(contract -> {
			YamlContract yamlContract = new YamlContract();
			if (contract == null) {
				return yamlContract;
			}
			yamlContract.name = contract.getName();
			yamlContract.ignored = contract.getIgnored();
			yamlContract.inProgress = contract.getInProgress();
			yamlContract.description = contract.getDescription();
			yamlContract.label = contract.getLabel();
			yamlContract.metadata = contract.getMetadata();
			yamlContract.priority = contract.getPriority();
			request(contract, yamlContract);
			response(yamlContract, contract);
			input(contract, yamlContract);
			output(contract, yamlContract);
			return yamlContract;
		}).collect(Collectors.toList());
	}

	protected void request(Contract contract, YamlContract yamlContract) {
		Request request = contract.getRequest();
		if (request != null) {
			ContentType requestContentType = evaluateClientSideContentType(request.getHeaders(), request.getBody());
			yamlContract.request = new YamlContract.Request();
			mapRequestMethod(yamlContract.request, request);
			mapRequestUrl(yamlContract.request, request);
			mapRequestUrlPath(yamlContract.request, request);
			mapRequestMatchers(yamlContract.request);
			Url requestUrl = Optional.ofNullable(request.getUrl()).orElse(request.getUrlPath());
			if (requestUrl.getQueryParameters() != null) {
				mapRequestQueryParameters(yamlContract.request, requestUrl);
				mapRequestMatchersQueryParameters(yamlContract.request, requestUrl);
			}
			mapRequestHeaders(yamlContract.request, request);
			mapRequestCookies(yamlContract.request, request);
			mapRequestBody(yamlContract.request, request);
			mapRequestMultipart(yamlContract.request, request);
			mapRequestMatchersBody(yamlContract.request, request);
			mapRequestMatchersUrl(yamlContract.request, request);
			mapRequestMatchersMultipart(yamlContract.request, request);

			// TODO: Cookie matchers - including absent
			if (XML != requestContentType) {
				setInputBodyMatchers(request.getBody(), yamlContract.request.matchers.body);
			}
			setInputHeadersMatchers(request.getHeaders(), yamlContract.request.matchers.headers);
		}
	}

	private void mapRequestMatchersMultipart(YamlContract.Request yamlContractRequest, Request request) {
		Multipart multipart = request.getMultipart();
		if (multipart != null) {
			yamlContractRequest.matchers.multipart = new YamlContract.MultipartStubMatcher();
			Map<String, Object> map = (Map<String, Object>) MapConverter.getStubSideValues(multipart);
			map.forEach((key, value) -> {
				if (value instanceof Part) {
					Object fileName = Optional.ofNullable(((Part) value).getFilename()).map(DslProperty::getClientValue)
							.orElse(null);
					Object fileContent = Optional.ofNullable(((Part) value).getBody()).map(DslProperty::getClientValue)
							.orElse(null);
					Object contentType = Optional.ofNullable(((Part) value).getContentType())
							.map(DslProperty::getClientValue).orElse(null);
					if (fileName instanceof RegexProperty || fileContent instanceof RegexProperty
							|| contentType instanceof RegexProperty) {
						YamlContract.MultipartNamedStubMatcher multipartNamedStubMatcher = new YamlContract.MultipartNamedStubMatcher();
						multipartNamedStubMatcher.paramName = key;
						multipartNamedStubMatcher.fileName = valueMatcher(fileName);
						multipartNamedStubMatcher.fileContent = valueMatcher(fileContent);
						multipartNamedStubMatcher.contentType = valueMatcher(contentType);
						yamlContractRequest.matchers.multipart.named.add(multipartNamedStubMatcher);
					}
				}
				else if (value instanceof RegexProperty || value instanceof Pattern) {
					RegexProperty property = new RegexProperty(value);
					YamlContract.KeyValueMatcher keyValueMatcher = new YamlContract.KeyValueMatcher();
					keyValueMatcher.key = key;
					keyValueMatcher.regex = property.pattern();
					keyValueMatcher.regexType = regexType(property.clazz());
					yamlContractRequest.matchers.multipart.params.add(keyValueMatcher);
				}
			});
		}
	}

	private void mapRequestMatchersUrl(YamlContract.Request yamlContractRequest, Request request) {
		Object url = Optional.ofNullable(request.getUrl()).map(Url::getClientValue).orElse(null);
		YamlContract.KeyValueMatcher keyValueMatcher = new YamlContract.KeyValueMatcher();
		if (url instanceof RegexProperty) {
			keyValueMatcher.regex = ((RegexProperty) url).pattern();
			yamlContractRequest.matchers.url = keyValueMatcher;
		}
		else if (url instanceof ExecutionProperty) {
			keyValueMatcher.command = url.toString();
			yamlContractRequest.matchers.url = keyValueMatcher;
		}
		else {
			yamlContractRequest.matchers.url = null;
		}

		Object urlPath = Optional.ofNullable(request.getUrlPath()).map(Url::getClientValue).orElse(null);
		if (urlPath instanceof RegexProperty) {
			keyValueMatcher.regex = ((RegexProperty) urlPath).pattern();
			yamlContractRequest.matchers.url = keyValueMatcher;
		}
		else if (urlPath instanceof ExecutionProperty) {
			keyValueMatcher.command = urlPath.toString();
			yamlContractRequest.matchers.url = keyValueMatcher;
		}
		else {
			yamlContractRequest.matchers.url = null;
		}
	}

	private void mapRequestMatchersBody(YamlContract.Request yamlContractRequest, Request request) {
		Optional.ofNullable(request.getBodyMatchers()).map(BodyMatchers::matchers)
				.ifPresent(bodyMatchers -> bodyMatchers.forEach(bodyMatcher -> {
					YamlContract.BodyStubMatcher bodyStubMatcher = new YamlContract.BodyStubMatcher();
					bodyStubMatcher.path = bodyMatcher.path();
					bodyStubMatcher.type = stubMatcherType(bodyMatcher.matchingType());
					bodyStubMatcher.value = Optional.ofNullable(bodyMatcher.value()).map(Object::toString).orElse(null);
					bodyStubMatcher.minOccurrence = bodyMatcher.minTypeOccurrence();
					bodyStubMatcher.maxOccurrence = bodyMatcher.maxTypeOccurrence();
					yamlContractRequest.matchers.body.add(bodyStubMatcher);
				}));
	}

	private void mapRequestMultipart(YamlContract.Request yamlContractRequest, Request request) {
		Multipart multipart = request.getMultipart();
		if (multipart != null) {
			yamlContractRequest.multipart = new YamlContract.Multipart();
			Map<String, Object> map = (Map<String, Object>) MapConverter.getTestSideValues(multipart);
			map.forEach((key, value) -> {
				if (value instanceof Part) {
					Object fileName = Optional.ofNullable(((Part) value).getFilename()).map(DslProperty::getServerValue)
							.orElse(null);
					Object contentType = Optional.ofNullable(((Part) value).getContentType())
							.map(DslProperty::getServerValue).orElse(null);
					Object fileContent = Optional.ofNullable(((Part) value).getBody()).map(DslProperty::getServerValue)
							.orElse(null);
					YamlContract.Named named = new YamlContract.Named();
					named.paramName = key;
					named.fileName = fileName instanceof String ? Optional.ofNullable(((Part) value).getFilename())
							.map(DslProperty::getServerValue).map(Object::toString).orElse(null) : null;
					named.fileContent = (String) Optional.ofNullable(fileContent).filter(f -> f instanceof String)
							.orElse(null);
					named.fileContentAsBytes = fileContent instanceof FromFileProperty
							? new String(((FromFileProperty) fileContent).asBytes()) : null;
					named.fileContentFromFileAsBytes = resolveFileNameAsBytes(fileContent);
					named.contentType = (String) Optional.ofNullable(contentType).filter(f -> f instanceof String)
							.orElse(null);
					named.fileNameCommand = fileName instanceof ExecutionProperty ? fileName.toString() : null;
					named.fileContentCommand = fileContent instanceof ExecutionProperty ? fileContent.toString() : null;
					named.contentTypeCommand = contentType instanceof ExecutionProperty ? contentType.toString() : null;
					yamlContractRequest.multipart.named.add(named);
				}
				else {
					yamlContractRequest.multipart.params.put(key, value != null ? value.toString() : null);
				}
			});
		}
	}

	private void mapRequestBody(YamlContract.Request yamlContractRequest, Request request) {
		Object body = Optional.ofNullable(request.getBody()).map(DslProperty::getServerValue).orElse(null);
		if (body instanceof FromFileProperty) {
			FromFileProperty fromFileProperty = (FromFileProperty) body;
			if (fromFileProperty.isByte()) {
				yamlContractRequest.bodyFromFileAsBytes = fromFileProperty.fileName();
			}
			if (fromFileProperty.isString()) {
				yamlContractRequest.bodyFromFile = fromFileProperty.fileName();
			}
		}
		else {
			yamlContractRequest.body = MapConverter.getTestSideValues(request.getBody());
		}
	}

	private void mapRequestCookies(YamlContract.Request yamlContractRequest, Request request) {
		yamlContractRequest.cookies = Optional.ofNullable(request.getCookies()).map(Cookies::asTestSideMap)
				.orElse(null);
	}

	private void mapRequestHeaders(YamlContract.Request yamlContractRequest, Request request) {
		yamlContractRequest.headers = request.getHeaders() != null ? request.getHeaders().asMap((headerName, prop) -> {
			Object testSideValue = MapConverter.getTestSideValues(prop);
			if (testSideValue instanceof ExecutionProperty) {
				return MapConverter.getStubSideValuesForNonBody(prop).toString();
			}
			return testSideValue.toString();
		}) : null;
	}

	private void mapRequestMatchersQueryParameters(YamlContract.Request yamlContractRequest, Url requestUrl) {
		yamlContractRequest.matchers.queryParameters
				.addAll(requestUrl.getQueryParameters().getParameters().stream().map(parameter -> {
					Object stubSide = parameter.getClientValue();
					if (stubSide instanceof RegexProperty || stubSide instanceof Pattern) {
						YamlContract.QueryParameterMatcher queryParameterMatcher = new YamlContract.QueryParameterMatcher();
						queryParameterMatcher.key = parameter.getName();
						queryParameterMatcher.type = YamlContract.MatchingType.matching;
						queryParameterMatcher.value = new RegexProperty(stubSide).pattern();
						return queryParameterMatcher;
					}
					else if (stubSide instanceof MatchingStrategy) {
						YamlContract.QueryParameterMatcher queryParameterMatcher = new YamlContract.QueryParameterMatcher();
						queryParameterMatcher.key = parameter.getName();
						queryParameterMatcher.type = YamlContract.MatchingType
								.from(((MatchingStrategy) stubSide).getType().getName());
						queryParameterMatcher.value = MapConverter.getStubSideValuesForNonBody(stubSide);
						return queryParameterMatcher;
					}
					else {
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	private void mapRequestQueryParameters(YamlContract.Request yamlContractRequest, Url requestUrl) {
		yamlContractRequest.queryParameters = requestUrl.getQueryParameters().getParameters().stream()
				.collect(Collectors.toMap(QueryParameter::getName, MapConverter::getTestSideValuesForNonBody));
	}

	private void mapRequestMatchers(YamlContract.Request yamlContractRequest) {
		yamlContractRequest.matchers = new YamlContract.StubMatchers();
	}

	private void mapRequestUrlPath(YamlContract.Request yamlContractRequest, Request request) {
		yamlContractRequest.urlPath = Optional.ofNullable(request.getUrlPath()).map(m -> m.getServerValue().toString())
				.orElse(null);
	}

	private void mapRequestUrl(YamlContract.Request yamlContractRequest, Request request) {
		yamlContractRequest.url = Optional.ofNullable(request.getUrl()).map(m -> m.getServerValue().toString())
				.orElse(null);
	}

	private void mapRequestMethod(YamlContract.Request yamlContractRequest, Request request) {
		yamlContractRequest.method = Optional.ofNullable(request.getMethod()).map(m -> m.getServerValue().toString())
				.orElse(null);
	}

	protected void output(Contract contract, YamlContract yamlContract) {
		OutputMessage outputMessage = contract.getOutputMessage();
		if (outputMessage != null) {
			Optional<Response> optionalResponse = Optional.ofNullable(contract.getResponse());
			ContentType contentType = evaluateClientSideContentType(
					optionalResponse.map(Response::getHeaders).orElse(null),
					optionalResponse.map(Response::getBody).orElse(null));
			yamlContract.outputMessage = new YamlContract.OutputMessage();
			yamlContract.outputMessage.sentTo = MapConverter.getStubSideValues(outputMessage.getSentTo()).toString();
			yamlContract.outputMessage.headers = Optional.ofNullable(outputMessage.getHeaders())
					.map(Headers::asStubSideMap).orElse(null);
			yamlContract.outputMessage.body = MapConverter.getStubSideValues(outputMessage.getBody());
			Optional.ofNullable(outputMessage.getBodyMatchers()).map(BodyMatchers::matchers)
					.ifPresent(bodyMatchers -> bodyMatchers.forEach(bodyMatcher -> {
						YamlContract.BodyTestMatcher bodyTestMatcher = new YamlContract.BodyTestMatcher();
						bodyTestMatcher.path = bodyMatcher.path();
						bodyTestMatcher.type = testMatcherType(bodyMatcher.matchingType());
						bodyTestMatcher.value = Optional.ofNullable(bodyMatcher.value()).map(Object::toString)
								.orElse(null);
						bodyTestMatcher.minOccurrence = bodyMatcher.minTypeOccurrence();
						bodyTestMatcher.maxOccurrence = bodyMatcher.maxTypeOccurrence();
						yamlContract.outputMessage.matchers.body.add(bodyTestMatcher);

					}));
			if (XML != contentType) {
				setOutputBodyMatchers(outputMessage.getBody(), yamlContract.outputMessage.matchers.body);
			}
			setOutputHeadersMatchers(outputMessage.getHeaders(), yamlContract.outputMessage.matchers.headers);
		}
	}

	protected void input(Contract contract, YamlContract yamlContract) {
		Input input = contract.getInput();
		if (input != null) {
			yamlContract.input = new YamlContract.Input();
			yamlContract.input.assertThat = Optional.ofNullable(input.getAssertThat())
					.map(assertThat -> MapConverter
							.getTestSideValues(assertThat.toString(), MapConverter.JSON_PARSING_FUNCTION).toString())
					.orElse(null);
			yamlContract.input.triggeredBy = Optional.ofNullable(input.getTriggeredBy())
					.map(triggeredBy -> MapConverter
							.getTestSideValues(triggeredBy.toString(), MapConverter.JSON_PARSING_FUNCTION).toString())
					.orElse(null);
		}
	}

	protected String resolveFileNameAsBytes(Object value) {
		if (!(value instanceof FromFileProperty)) {
			return null;
		}
		FromFileProperty property = (FromFileProperty) value;
		return property.fileName();
	}

	protected YamlContract.ValueMatcher valueMatcher(Object o) {
		return Optional.ofNullable(o).filter(object -> object instanceof RegexProperty)
				.map(object -> (RegexProperty) object).map(regexProperty -> {
					YamlContract.ValueMatcher valueMatcher = new YamlContract.ValueMatcher();
					valueMatcher.regex = regexProperty.pattern();
					return valueMatcher;
				}).orElse(null);
	}

	protected void setInputBodyMatchers(DslProperty<?> body, List<YamlContract.BodyStubMatcher> bodyMatchers) {
		Object testSideValues = MapConverter.getTestSideValues(body);
		JsonPaths paths = new JsonToJsonPathsConverter().transformToJsonPathWithStubsSideValues(body);
		paths.stream().filter((path) -> path.valueBeforeChecking() instanceof Pattern).forEach((path) -> {
			Object element = JsonToJsonPathsConverter.readElement(testSideValues, path.keyBeforeChecking());
			YamlContract.BodyStubMatcher bodyStubMatcher = new YamlContract.BodyStubMatcher();
			bodyStubMatcher.path = path.keyBeforeChecking();
			bodyStubMatcher.type = YamlContract.StubMatcherType.by_regex;
			bodyStubMatcher.value = ((Pattern) path.valueBeforeChecking()).pattern();
			bodyStubMatcher.regexType = regexType(element);
			bodyMatchers.add(bodyStubMatcher);
		});
	}

	protected YamlContract.RegexType regexType(Object from) {
		return regexType(from.getClass());
	}

	protected YamlContract.RegexType regexType(Class<?> clazz) {
		return PRIMITIVE_WRAPPER_TO_REGEX_TYPE.getOrDefault(clazz, PRIMITIVE_WRAPPER_TO_REGEX_TYPE.get(String.class));
	}

	protected void response(YamlContract yamlContract, Contract contract) {
		if (contract.getResponse() != null) {
			Response contractResponse = contract.getResponse();
			ContentType contentType = evaluateClientSideContentType(contractResponse.getHeaders(),
					contractResponse.getBody());
			YamlContract.Response response = new YamlContract.Response();
			yamlContract.response = response;
			mapResponseAsync(contractResponse, response);
			mapResponseFixedDelayMilliseconds(contractResponse, response);
			mapResponseStatus(contractResponse, response);
			mapResponseHeaders(contractResponse, response);
			mapResponseCookies(contractResponse, response);
			mapResponseBody(contractResponse, response);
			mapResponseBodyMatchers(contractResponse, response);
			if (XML != contentType) {
				setOutputBodyMatchers(contractResponse.getBody(), yamlContract.response.matchers.body);
			}
			setOutputHeadersMatchers(contractResponse.getHeaders(), yamlContract.response.matchers.headers);
		}
	}

	private void mapResponseBodyMatchers(Response contractResponse, YamlContract.Response response) {
		Optional.ofNullable(contractResponse.getBodyMatchers()).map(BodyMatchers::matchers)
				.ifPresent(bodyMatchers -> bodyMatchers.forEach((bodyMatcher) -> {
					YamlContract.BodyTestMatcher bodyTestMatcher = new YamlContract.BodyTestMatcher();
					bodyTestMatcher.path = bodyMatcher.path();
					bodyTestMatcher.type = testMatcherType(bodyMatcher.matchingType());
					bodyTestMatcher.value = Optional.ofNullable(bodyMatcher.value()).map(Object::toString).orElse(null);
					bodyTestMatcher.minOccurrence = bodyMatcher.minTypeOccurrence();
					bodyTestMatcher.maxOccurrence = bodyMatcher.maxTypeOccurrence();
					response.matchers.body.add(bodyTestMatcher);
				}));
	}

	private void mapResponseBody(Response contractResponse, YamlContract.Response response) {
		Object body = Optional.ofNullable(contractResponse.getBody()).map(DslProperty::getClientValue).orElse(null);
		if (body instanceof FromFileProperty) {
			if (((FromFileProperty) body).isByte()) {
				response.bodyFromFileAsBytes = ((FromFileProperty) body).fileName();
			}
			if (((FromFileProperty) body).isString()) {
				response.bodyFromFile = ((FromFileProperty) body).fileName();
			}
		}
		else {
			response.body = MapConverter.getStubSideValues(contractResponse.getBody());
		}
	}

	private void mapResponseCookies(Response contractResponse, YamlContract.Response response) {
		response.cookies = Optional.ofNullable(contractResponse.getCookies()).map(Cookies::asStubSideMap).orElse(null);
	}

	private void mapResponseHeaders(Response contractResponse, YamlContract.Response response) {
		response.headers = Optional.ofNullable(contractResponse.getHeaders())
				.map(headers -> headers.asMap((headerName, dslProperty) -> MapConverter.getStubSideValues(dslProperty)))
				.orElse(null);
	}

	private void mapResponseStatus(Response contractResponse, YamlContract.Response response) {
		response.status = Optional.ofNullable(contractResponse.getStatus()).map(DslProperty::getClientValue)
				.map(clientValue -> (Integer) clientValue).orElse(null);
	}

	private void mapResponseFixedDelayMilliseconds(Response contractResponse, YamlContract.Response response) {
		response.fixedDelayMilliseconds = (Integer) Optional.ofNullable(contractResponse.getDelay())
				.map(DslProperty::getClientValue).orElse(null);
	}

	private void mapResponseAsync(Response contractResponse, YamlContract.Response response) {
		response.async = contractResponse.getAsync();
	}

	protected void setOutputBodyMatchers(DslProperty<?> body, List<YamlContract.BodyTestMatcher> bodyMatchers) {
		Object testSideValues = MapConverter.getTestSideValues(body);
		JsonPaths paths = new JsonToJsonPathsConverter().transformToJsonPathWithTestsSideValues(body);
		paths.stream().filter(m -> m.valueBeforeChecking() instanceof Pattern).forEach((m) -> {
			Object element = JsonToJsonPathsConverter.readElement(testSideValues, m.keyBeforeChecking());
			YamlContract.BodyTestMatcher bodyTestMatcher = new YamlContract.BodyTestMatcher();
			bodyTestMatcher.path = m.keyBeforeChecking();
			bodyTestMatcher.type = YamlContract.TestMatcherType.by_regex;
			bodyTestMatcher.value = ((Pattern) m.valueBeforeChecking()).pattern();
			bodyTestMatcher.regexType = regexType(element);
			bodyMatchers.add(bodyTestMatcher);
		});
		Optional.ofNullable(body).filter(b -> b.getServerValue() instanceof Pattern).ifPresent((b) -> {
			YamlContract.BodyTestMatcher bodyTestMatcher = new YamlContract.BodyTestMatcher();
			bodyTestMatcher.type = YamlContract.TestMatcherType.by_regex;
			bodyTestMatcher.value = ((Pattern) b.getServerValue()).pattern();
			bodyMatchers.add(bodyTestMatcher);
		});
	}

	protected void setInputHeadersMatchers(Headers headers, List<YamlContract.KeyValueMatcher> headerMatchers) {
		Optional.ofNullable(headers).map(Headers::asStubSideMap)
				.ifPresent(stubSideMap -> stubSideMap.forEach((key, value) -> {
					if (value instanceof RegexProperty || value instanceof Pattern) {
						RegexProperty property = new RegexProperty(value);
						YamlContract.KeyValueMatcher keyValueMatcher = new YamlContract.KeyValueMatcher();
						keyValueMatcher.key = key;
						keyValueMatcher.regex = property.pattern();
						keyValueMatcher.regexType = regexType(property.clazz());
						headerMatchers.add(keyValueMatcher);
					}
				}));
	}

	protected void setOutputHeadersMatchers(Headers headers, List<YamlContract.TestHeaderMatcher> headerMatchers) {
		Optional.ofNullable(headers).map(Headers::asTestSideMap)
				.ifPresent(testSideMap -> testSideMap.forEach((key, value) -> {
					if (value instanceof RegexProperty || value instanceof Pattern) {
						RegexProperty property = new RegexProperty(value);
						YamlContract.TestHeaderMatcher testHeaderMatcher = new YamlContract.TestHeaderMatcher();
						testHeaderMatcher.key = key;
						testHeaderMatcher.regex = property.pattern();
						testHeaderMatcher.regexType = regexType(property.clazz());
						headerMatchers.add(testHeaderMatcher);
					}
					else if (value instanceof ExecutionProperty) {
						YamlContract.TestHeaderMatcher testHeaderMatcher = new YamlContract.TestHeaderMatcher();
						testHeaderMatcher.key = key;
						testHeaderMatcher.command = ((ExecutionProperty) value).getExecutionCommand();
						headerMatchers.add(testHeaderMatcher);
					}
					else if (value instanceof NotToEscapePattern) {
						YamlContract.TestHeaderMatcher testHeaderMatcher = new YamlContract.TestHeaderMatcher();
						testHeaderMatcher.key = key;
						testHeaderMatcher.regex = (((NotToEscapePattern) value).getServerValue()).pattern();
						headerMatchers.add(testHeaderMatcher);
					}
				}));
	}

	protected YamlContract.TestMatcherType testMatcherType(MatchingType matchingType) {
		return TEST_MATCHER_TYPE.getOrDefault(matchingType, null);
	}

	protected YamlContract.StubMatcherType stubMatcherType(MatchingType matchingType) {
		if (matchingType == MatchingType.COMMAND || matchingType == MatchingType.TYPE) {
			throw new UnsupportedOperationException("No type or command for client side");
		}
		return STUB_MATCHER_TYPE.getOrDefault(matchingType, null);
	}

}
