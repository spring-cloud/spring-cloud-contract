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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.collections.MapUtils;
import org.yaml.snakeyaml.Yaml;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.spec.internal.MatchingTypeValue;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.spec.internal.RegexPatterns;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Response;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.cloud.contract.verifier.util.NamesUtil;
import org.springframework.util.StringUtils;

import static java.util.stream.Collectors.toSet;
import static org.springframework.cloud.contract.verifier.util.ContentType.XML;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateClientSideContentType;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 */
class YamlToContracts {

	Collection<Contract> convertFrom(File contractFile) {
		ClassLoader classLoader = YamlContractConverter.class.getClassLoader();
		YAMLMapper mapper = new YAMLMapper();
		try {
			Iterable<Object> iterables = new Yaml().loadAll(Files.newInputStream(contractFile.toPath()));
			Collection<Contract> contracts = new ArrayList<>();
			int counter = 0;
			for (Object document : iterables) {
				List<Contract> processedYaml = processYaml(counter, document, mapper, classLoader, contractFile);
				contracts.addAll(processedYaml);
				counter = counter + 1;
			}
			return contracts;
		}
		catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
		catch (IllegalStateException ise) {
			throw ise;
		}
		catch (Exception e1) {
			throw new IllegalStateException("Exception occurred while processing the file [" + contractFile + "]", e1);
		}
		finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	protected List<Contract> processYaml(int counter, Object document, ObjectMapper mapper, ClassLoader classLoader,
			File contractFile) {
		List<YamlContract> yamlContracts = convert(mapper, document);
		Thread.currentThread().setContextClassLoader(updatedClassLoader(contractFile.getParentFile(), classLoader));
		List<Contract> contracts = new ArrayList<>();
		for (YamlContract yamlContract : yamlContracts) {
			Contract contract = Contract.make((dslContract) -> {
				mapDescription(yamlContract, dslContract);
				mapLabel(yamlContract, dslContract);
				mapName(counter, contractFile, yamlContracts, yamlContract, dslContract);
				mapPriority(yamlContract, dslContract);
				mapIgnored(yamlContract, dslContract);
				mapInProgress(yamlContract, dslContract);
				mapMetadata(yamlContract, dslContract);
				mapRequest(yamlContract, dslContract);
				mapResponse(yamlContract, dslContract);
				mapInput(yamlContract, dslContract);
				mapOutput(yamlContract, dslContract);
			});
			contracts.add(contract);
		}
		return contracts;
	}

	private void mapMetadata(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.metadata != null) {
			dslContract.metadata(yamlContract.metadata);
		}
	}

	private void mapInProgress(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.inProgress) {
			dslContract.inProgress();
		}
	}

	private void mapIgnored(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.ignored) {
			dslContract.ignored();
		}
	}

	private void mapPriority(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.priority != null) {
			dslContract.priority(yamlContract.priority);
		}
	}

	private void mapName(int counter, File contractFile, List<YamlContract> yamlContracts, YamlContract yamlContract,
			Contract dslContract) {
		dslContract.name(StringUtils.hasText(yamlContract.name) ? yamlContract.name
				: NamesUtil.defaultContractName(contractFile, yamlContracts, counter));
	}

	private void mapLabel(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.label != null) {
			dslContract.label(yamlContract.label);
		}
	}

	private void mapDescription(YamlContract yamlContract, Contract dslContract) {
		if (yamlContract.description != null) {
			dslContract.description(yamlContract.description);
		}
	}

	private void mapRequest(YamlContract yamlContract, Contract dslContract) {
		YamlContract.Request yamlContractRequest = yamlContract.request;
		if (yamlContractRequest != null) {
			dslContract.request((dslContractRequest) -> {
				mapRequestMethod(yamlContractRequest, dslContractRequest);
				mapRequestUrl(yamlContract, dslContractRequest);
				mapRequestUrlPath(yamlContract, dslContractRequest);
				mapRequestHeaders(yamlContractRequest, dslContractRequest);
				mapRequestCookies(yamlContractRequest, dslContractRequest);
				mapRequestBody(yamlContractRequest, dslContractRequest);
				mapRequestMultiPart(yamlContractRequest, dslContractRequest);
				mapRequestBodyMatchers(yamlContractRequest, dslContractRequest);
			});
		}
	}

	private void mapRequestMethod(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		if (yamlContractRequest.method != null) {
			dslContractRequest.method(yamlContractRequest.method);
		}
	}

	private void mapRequestUrl(YamlContract yamlContract, Request dslContractRequest) {
		String yamlContractRequestUrl = yamlContract.request.url;
		if (yamlContractRequestUrl != null) {
			YamlContract.KeyValueMatcher yamlContractRequestMatchersUrl = Optional
					.ofNullable(yamlContract.request.matchers).map(matchers -> matchers.url).orElse(null);
			dslContractRequest.url(urlValue(yamlContractRequestUrl, yamlContractRequestMatchersUrl),
					url -> handleQueryParameters(yamlContract, url));
		}
	}

	private void mapRequestUrlPath(YamlContract yamlContract, Request dslContractRequest) {
		String yamlContractRequestUrlPath = yamlContract.request.urlPath;
		if (yamlContractRequestUrlPath != null) {
			YamlContract.KeyValueMatcher yamlContractRequestMatchersUrl = Optional
					.ofNullable(yamlContract.request.matchers).map(matchers -> matchers.url).orElse(null);
			dslContractRequest.urlPath(urlValue(yamlContractRequestUrlPath, yamlContractRequestMatchersUrl),
					urlPath -> handleQueryParameters(yamlContract, urlPath));
		}
	}

	private void handleQueryParameters(YamlContract yamlContract, Url url) {
		if (yamlContract.request.queryParameters != null) {
			url.queryParameters((queryParameters -> yamlContract.request.queryParameters.forEach((key, value) -> {
				if (value instanceof List) {
					((List<?>) value)
							.forEach(v -> queryParameters.parameter(key, queryParamValue(yamlContract, key, v)));
				}
				else {
					queryParameters.parameter(key, queryParamValue(yamlContract, key, value));
				}
			})));
		}
	}

	private void mapRequestHeaders(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		Map<String, Object> yamlContractRequestHeaders = yamlContractRequest.headers;
		if (MapUtils.isNotEmpty(yamlContractRequestHeaders)) {
			dslContractRequest.headers((headers) -> yamlContractRequestHeaders.forEach((key, value) -> {
				List<YamlContract.KeyValueMatcher> matchers = yamlContractRequest.matchers.headers.stream()
						.filter((header) -> header.key.equals(key)).collect(Collectors.toList());
				matchers.forEach(matcher -> {
					if (value instanceof List) {
						((List<?>) value)
								.forEach(v -> headers.header(key, clientValue(v, matcher, key).getClientValue()));
					}
					else {
						headers.header(key, new DslProperty<>(clientValue(value, matcher, key).getClientValue(),
								serverValue(value, matcher)));
					}
				});
				if (matchers.isEmpty()) {
					headers.header(key, value);
				}
			}));
		}
	}

	private void mapRequestCookies(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		Map<String, Object> yamlContractRequestCookies = yamlContractRequest.cookies;
		if (MapUtils.isNotEmpty(yamlContractRequestCookies)) {
			dslContractRequest.cookies((cookies) -> yamlContractRequestCookies.forEach((key, value) -> {
				YamlContract.KeyValueMatcher matcher = yamlContractRequest.matchers.cookies.stream()
						.filter(cookie -> cookie.key.equals(key)).findFirst().orElse(null);
				cookies.cookie(key, clientValue(value, matcher, key));
			}));
		}
	}

	private void mapRequestBody(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		if (yamlContractRequest.body != null) {
			dslContractRequest.body(yamlContractRequest.body);
		}
		if (yamlContractRequest.bodyFromFile != null) {
			dslContractRequest.body(file(yamlContractRequest.bodyFromFile));
		}
		if (yamlContractRequest.bodyFromFileAsBytes != null) {
			dslContractRequest.body(dslContractRequest.fileAsBytes(yamlContractRequest.bodyFromFileAsBytes));
		}
	}

	private void mapRequestMultiPart(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		if (yamlContractRequest.multipart != null) {
			Map<String, Object> multipartMap = new HashMap<>();
			yamlContractRequest.multipart.params.forEach((paramKey, paramValue) -> {
				YamlContract.KeyValueMatcher matcher = yamlContractRequest.matchers.multipart.params.stream()
						.filter((param) -> param.key.equals(paramKey)).findFirst().orElse(null);
				Object value = paramValue;
				if (matcher != null) {
					value = matcher.regex != null ? Pattern.compile(matcher.regex)
							: predefinedToPattern(matcher.predefined);
				}
				multipartMap.put(paramKey, new DslProperty<>(value, paramValue));
			});
			yamlContractRequest.multipart.named.forEach(namedParam -> {
				YamlContract.MultipartNamedStubMatcher matcher = yamlContractRequest.matchers.multipart.named.stream()
						.filter((stubMatcher) -> stubMatcher.paramName.equals(namedParam.paramName)).findFirst()
						.orElse(null);
				Object fileNameValue = namedParam.fileName;
				Object fileContentValue = namedParam.fileContent;
				String fileContentAsBytes = namedParam.fileContentAsBytes;
				String fileContentFromFileAsBytes = namedParam.fileContentFromFileAsBytes;
				String contentTypeCommand = namedParam.contentTypeCommand;
				String fileContentCommand = namedParam.fileContentCommand;
				String fileNameCommand = namedParam.fileNameCommand;
				Object contentTypeValue = namedParam.contentType;
				if (matcher != null && matcher.fileName != null) {
					fileNameValue = matcher.fileName.regex != null ? Pattern.compile(matcher.fileName.regex)
							: predefinedToPattern(matcher.fileName.predefined);
				}
				if (matcher != null && matcher.fileContent != null) {
					fileContentValue = matcher.fileContent.regex != null ? Pattern.compile(matcher.fileContent.regex)
							: predefinedToPattern(matcher.fileContent.predefined);
				}
				if (matcher != null && matcher.contentType != null) {
					contentTypeValue = matcher.contentType.regex != null ? Pattern.compile(matcher.contentType.regex)
							: predefinedToPattern(matcher.contentType.predefined);
				}
				multipartMap.put(namedParam.paramName, new NamedProperty(
						new DslProperty<>(fileNameValue,
								fileNameCommand != null ? new ExecutionProperty(fileNameCommand) : namedParam.fileName),
						new DslProperty<>(fileContentValue,
								namedParam.fileContent != null ? namedParam.fileContent
										: fileContentFromFileAsBytes != null
												? dslContractRequest.fileAsBytes(namedParam.fileContentFromFileAsBytes)
												: fileContentAsBytes != null ? fileContentAsBytes.getBytes()
														: new ExecutionProperty(fileContentCommand)),
						new DslProperty<>(contentTypeValue, contentTypeCommand != null
								? new ExecutionProperty(contentTypeCommand) : namedParam.contentType)));
			});
			dslContractRequest.multipart(multipartMap);
		}

	}

	private void mapRequestBodyMatchers(YamlContract.Request yamlContractRequest, Request dslContractRequest) {
		dslContractRequest.bodyMatchers((bodyMatchers) -> Optional.ofNullable(yamlContractRequest.matchers)
				.map(stubMatchers -> stubMatchers.body).ifPresent(stubMatchers -> stubMatchers.forEach(stubMatcher -> {
					ContentType contentType = evaluateClientSideContentType(
							yamlHeadersToContractHeaders(
									Optional.ofNullable(yamlContractRequest.headers).orElse(new HashMap<>())),
							Optional.ofNullable(yamlContractRequest.body).orElse(null));
					MatchingTypeValue value = null;
					switch (stubMatcher.type) {
					case by_date:
						value = bodyMatchers.byDate();
						break;
					case by_time:
						value = bodyMatchers.byTime();
						break;
					case by_timestamp:
						value = bodyMatchers.byTimestamp();
						break;
					case by_regex:
						String regex = stubMatcher.value;
						if (stubMatcher.predefined != null) {
							regex = predefinedToPattern(stubMatcher.predefined).pattern();
						}
						value = bodyMatchers.byRegex(regex);
						break;
					case by_equality:
						value = bodyMatchers.byEquality();
						break;
					case by_type:
						value = bodyMatchers.byType(matchingTypeValueHolder -> {
							if (stubMatcher.minOccurrence != null) {
								matchingTypeValueHolder.minOccurrence(stubMatcher.minOccurrence);
							}
							if (stubMatcher.maxOccurrence != null) {
								matchingTypeValueHolder.maxOccurrence(stubMatcher.maxOccurrence);
							}
						});
						break;
					case by_null:
						// do nothing
						break;
					default:
						throw new UnsupportedOperationException("The type [" + stubMatcher.type + "] is"
								+ " unsupported.Hint:If you 're using <predefined> remember to pass <type:by_regex > ");
					}
					if (value != null) {
						if (XML == contentType) {
							bodyMatchers.xPath(stubMatcher.path, value);
						}
						else {
							bodyMatchers.jsonPath(stubMatcher.path, value);
						}
					}
				})));
	}

	private void mapResponse(YamlContract yamlContract, Contract dslContract) {
		YamlContract.Response yamlContractResponse = yamlContract.response;
		if (yamlContractResponse != null) {
			dslContract.response(dslContractResponse -> {
				mapResponseStatus(yamlContractResponse, dslContractResponse);
				mapResponseHeaders(yamlContractResponse, dslContractResponse);
				mapResponseCookies(yamlContractResponse, dslContractResponse);
				mapResponseBody(yamlContractResponse, dslContractResponse);
				mapResponseAsync(yamlContractResponse, dslContractResponse);
				mapResponseFixedDelayMilliseconds(yamlContractResponse, dslContractResponse);
				mapResponseBodyMatchers(yamlContractResponse, dslContractResponse);
			});
		}

	}

	private void mapResponseStatus(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		dslContractResponse.status(yamlContractResponse.status);
	}

	private void mapResponseHeaders(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		dslContractResponse.headers(headers -> Optional.ofNullable(yamlContractResponse.headers)
				.ifPresent(yamlContractResponseHeaders -> yamlContractResponseHeaders.forEach((key, value) -> {
					YamlContract.TestHeaderMatcher matcher = yamlContractResponse.matchers.headers.stream()
							.filter(h -> h.key.equals(key)).findFirst().orElse(null);

					if (value instanceof List) {
						((List<?>) value).forEach(v -> {
							Object serverValue = serverValue(v, matcher, key);
							headers.header(key, new DslProperty<>(v, serverValue));
						});
					}
					else {
						Object serverValue = serverValue(value, matcher, key);
						headers.header(key, new DslProperty<>(value, serverValue));
					}
				})));
	}

	private void mapResponseCookies(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		if (yamlContractResponse.cookies != null) {
			dslContractResponse.cookies(cookies -> yamlContractResponse.cookies.forEach((key, value) -> {
				YamlContract.TestCookieMatcher matcher = yamlContractResponse.matchers.cookies.stream()
						.filter(testCookieMatcher -> testCookieMatcher.key.equals(key)).findFirst().orElse(null);
				DslProperty<?> cookieValue = serverCookieValue(value, matcher, key);
				cookies.cookie(key, cookieValue);
			}));
		}
	}

	private void mapResponseBody(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		if (yamlContractResponse.body != null) {
			YamlContract.BodyTestMatcher bodyTestMatcher = Optional.ofNullable(yamlContractResponse.matchers)
					.map(testMatchers -> testMatchers.body)
					.flatMap(
							bodyTestMatchers -> bodyTestMatchers.stream()
									.filter(m -> m.path == null && (m.type == YamlContract.TestMatcherType.by_regex
											|| m.type == YamlContract.TestMatcherType.by_command))
									.findFirst())
					.orElse(null);
			if (bodyTestMatcher != null) {
				dslContractResponse.body(new DslProperty<>(yamlContractResponse.body,
						bodyTestMatcher.type == YamlContract.TestMatcherType.by_regex
								? Pattern.compile(bodyTestMatcher.value)
								: new ExecutionProperty(bodyTestMatcher.value)));
			}
			else {
				dslContractResponse.body(yamlContractResponse.body);
			}
		}
		if (yamlContractResponse.bodyFromFile != null) {
			dslContractResponse.body(file(yamlContractResponse.bodyFromFile));
		}
		if (yamlContractResponse.bodyFromFileAsBytes != null) {
			dslContractResponse.body(dslContractResponse.fileAsBytes(yamlContractResponse.bodyFromFileAsBytes));
		}
	}

	private void mapResponseAsync(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		if (yamlContractResponse.async != null && yamlContractResponse.async) {
			dslContractResponse.async();
		}
	}

	private void mapResponseFixedDelayMilliseconds(YamlContract.Response yamlContractResponse,
			Response dslContractResponse) {
		if (yamlContractResponse.fixedDelayMilliseconds != null) {
			dslContractResponse.async();
			dslContractResponse.fixedDelayMilliseconds(yamlContractResponse.fixedDelayMilliseconds);
		}
	}

	private void mapResponseBodyMatchers(YamlContract.Response yamlContractResponse, Response dslContractResponse) {
		dslContractResponse.bodyMatchers(bodyMatchers -> Optional.ofNullable(yamlContractResponse.matchers)
				.map(yamlContractResponseTestMatchers -> yamlContractResponseTestMatchers.body)
				.ifPresent(yamlContractBodyTestMatchers -> yamlContractBodyTestMatchers
						.forEach(yamlContractBodyTestMatcher -> {
							ContentType contentType = evaluateClientSideContentType(
									yamlHeadersToContractHeaders(yamlContractResponse.headers),
									yamlContractResponse.body);
							MatchingTypeValue value;
							switch (yamlContractBodyTestMatcher.type) {
							case by_date:
								value = bodyMatchers.byDate();
								break;
							case by_time:
								value = bodyMatchers.byTime();
								break;
							case by_timestamp:
								value = bodyMatchers.byTimestamp();
								break;
							case by_regex:
								String regex = yamlContractBodyTestMatcher.value;
								if (yamlContractBodyTestMatcher.predefined != null) {
									regex = predefinedToPattern(yamlContractBodyTestMatcher.predefined).pattern();
								}
								value = bodyMatchers.byRegex(regex);
								break;
							case by_equality:
								value = bodyMatchers.byEquality();
								break;
							case by_type:
								value = bodyMatchers.byType(v -> {
									if (yamlContractBodyTestMatcher.minOccurrence != null) {
										v.minOccurrence(yamlContractBodyTestMatcher.minOccurrence);
									}
									if (yamlContractBodyTestMatcher.maxOccurrence != null) {
										v.maxOccurrence(yamlContractBodyTestMatcher.maxOccurrence);
									}
								});
								break;
							case by_command:
								value = bodyMatchers.byCommand(yamlContractBodyTestMatcher.value);
								break;
							case by_null:
								value = bodyMatchers.byNull();
								break;
							default:
								throw new UnsupportedOperationException("The type [" + yamlContractBodyTestMatcher.type
										+ "] is unsupported. "
										+ "Hint: If you're using <predefined> remember to pass < type:by_regex > ");
							}
							if (yamlContractBodyTestMatcher.path != null) {
								if (XML == contentType) {
									bodyMatchers.xPath(yamlContractBodyTestMatcher.path, value);
								}
								else {
									bodyMatchers.jsonPath(yamlContractBodyTestMatcher.path, value);
								}
							}
						})));
	}

	private void mapOutput(YamlContract yamlContract, Contract dslContract) {
		YamlContract.OutputMessage yamlContractOutputMessage = yamlContract.outputMessage;
		if (yamlContract.outputMessage != null) {
			dslContract.outputMessage((dslContractOutputMessage) -> {
				mapOutputAssertThat(yamlContractOutputMessage, dslContractOutputMessage);
				mapOutputSentTo(yamlContractOutputMessage, dslContractOutputMessage);
				mapOutputMessageHeaders(yamlContractOutputMessage, dslContractOutputMessage);
				mapOutputBody(yamlContractOutputMessage, dslContractOutputMessage);
				mapOutputBodyMatchers(yamlContractOutputMessage, dslContractOutputMessage);
			});
		}
	}

	private void mapOutputBodyMatchers(YamlContract.OutputMessage yamlContractOutputMessage,
			OutputMessage dslContractOutputMessage) {
		if (yamlContractOutputMessage.matchers != null) {
			dslContractOutputMessage.bodyMatchers(
					dslContractOutputMessageBodyMatchers -> Optional.ofNullable(yamlContractOutputMessage.matchers.body)
							.ifPresent(yamlContractBodyTestMatchers -> yamlContractBodyTestMatchers
									.forEach(yamlContractBodyTestMatcher -> {
										ContentType contentType = evaluateClientSideContentType(
												yamlHeadersToContractHeaders(yamlContractOutputMessage.headers),
												yamlContractOutputMessage.body);
										MatchingTypeValue value;
										switch (yamlContractBodyTestMatcher.type) {
										case by_date:
											value = dslContractOutputMessageBodyMatchers.byDate();
											break;
										case by_time:
											value = dslContractOutputMessageBodyMatchers.byTime();
											break;
										case by_timestamp:
											value = dslContractOutputMessageBodyMatchers.byTimestamp();
											break;
										case by_regex:
											String regex = yamlContractBodyTestMatcher.value;
											if (yamlContractBodyTestMatcher.predefined != null) {
												regex = predefinedToPattern(yamlContractBodyTestMatcher.predefined)
														.pattern();
											}
											value = dslContractOutputMessageBodyMatchers.byRegex(regex);
											break;
										case by_equality:
											value = dslContractOutputMessageBodyMatchers.byEquality();
											break;
										case by_type:
											value = dslContractOutputMessageBodyMatchers.byType(v -> {
												if (yamlContractBodyTestMatcher.minOccurrence != null) {
													v.minOccurrence(yamlContractBodyTestMatcher.minOccurrence);
												}
												if (yamlContractBodyTestMatcher.maxOccurrence != null) {
													v.maxOccurrence(yamlContractBodyTestMatcher.maxOccurrence);
												}
											});
											break;
										case by_command:
											value = dslContractOutputMessageBodyMatchers
													.byCommand(yamlContractBodyTestMatcher.value);
											break;
										case by_null:
											value = dslContractOutputMessageBodyMatchers.byNull();
											break;
										default:
											throw new UnsupportedOperationException("The type " + "["
													+ yamlContractBodyTestMatcher.type + "] is unsupported. Hint: If "
													+ "you're using <predefined> remember to pass < type:by_regex > ");
										}
										if (XML == contentType) {
											dslContractOutputMessageBodyMatchers.xPath(yamlContractBodyTestMatcher.path,
													value);
										}
										else {
											dslContractOutputMessageBodyMatchers
													.jsonPath(yamlContractBodyTestMatcher.path, value);
										}
									})));
		}
	}

	private void mapOutputMessageHeaders(YamlContract.OutputMessage yamlContractOutputMessage,
			OutputMessage dslContractOutputMessage) {
		dslContractOutputMessage.headers(dslContractOutputMessageHeaders -> Optional
				.ofNullable(yamlContractOutputMessage).map(yamlContractOutput -> yamlContractOutput.headers).ifPresent(
						yamlContractOutputMessageHeaders -> yamlContractOutputMessageHeaders.forEach((key, value) -> {
							YamlContract.TestHeaderMatcher matcher = Optional
									.ofNullable(yamlContractOutputMessage.matchers)
									.map(yamlContractOutputMatchers -> yamlContractOutputMatchers.headers)
									.flatMap(yamlContractOutputMatchersHeaders -> yamlContractOutputMatchersHeaders
											.stream()
											.filter(yamlContractOutputMatchersHeader -> yamlContractOutputMatchersHeader.key
													.equals(key))
											.findFirst())
									.orElse(null);
							Object serverValue = serverValue(value, matcher, key);
							dslContractOutputMessageHeaders.header(key, new DslProperty<>(value, serverValue));
						})));
	}

	private void mapOutputBody(YamlContract.OutputMessage yamlContractOutputMessage,
			OutputMessage dslContractOutputMessage) {
		if (yamlContractOutputMessage.body != null) {
			dslContractOutputMessage.body(yamlContractOutputMessage.body);
		}
		if (yamlContractOutputMessage.bodyFromFile != null) {
			dslContractOutputMessage.body(file(yamlContractOutputMessage.bodyFromFile));
		}
		if (yamlContractOutputMessage.bodyFromFileAsBytes != null) {
			dslContractOutputMessage
					.body(dslContractOutputMessage.fileAsBytes(yamlContractOutputMessage.bodyFromFileAsBytes));
		}
	}

	private void mapOutputSentTo(YamlContract.OutputMessage yamlContractOutputMessage,
			OutputMessage dslContractOutputMessage) {
		if (yamlContractOutputMessage.sentTo != null) {
			dslContractOutputMessage.sentTo(yamlContractOutputMessage.sentTo);
		}
	}

	private void mapOutputAssertThat(YamlContract.OutputMessage yamlContractOutputMessage,
			OutputMessage dslContractOutputMessage) {
		if (yamlContractOutputMessage.assertThat != null) {
			dslContractOutputMessage.assertThat(yamlContractOutputMessage.assertThat);
		}
	}

	private void mapInput(YamlContract yamlContract, Contract dslContract) {
		YamlContract.Input yamlContractInput = yamlContract.input;
		if (yamlContractInput != null) {
			dslContract.input(dslContractInput -> {
				mapInputMessageFrom(yamlContractInput, dslContractInput);
				mapInputAssertThat(yamlContractInput, dslContractInput);
				mapInputTriggeredBy(yamlContractInput, dslContractInput);
				mapInputMessageHeaders(yamlContractInput, dslContractInput);
				mapInputMessageBody(yamlContractInput, dslContractInput);
				mapInputBodyMatchers(yamlContractInput, dslContractInput);
			});
		}

	}

	private void mapInputMessageFrom(YamlContract.Input yamlContractInput, Input dslContractInput) {
		if (yamlContractInput.messageFrom != null) {
			dslContractInput.messageFrom(yamlContractInput.messageFrom);
		}
	}

	private void mapInputAssertThat(YamlContract.Input yamlContractInput, Input dslContractInput) {
		if (yamlContractInput.assertThat != null) {
			dslContractInput.assertThat(yamlContractInput.assertThat);
		}
	}

	private void mapInputTriggeredBy(YamlContract.Input yamlContractInput, Input dslContractInput) {
		if (yamlContractInput.triggeredBy != null) {
			dslContractInput.triggeredBy(yamlContractInput.triggeredBy);
		}
	}

	private void mapInputMessageHeaders(YamlContract.Input yamlContractInput, Input dslContractInput) {
		dslContractInput
				.messageHeaders(dslContractMessageHeaders -> Optional.ofNullable(yamlContractInput.messageHeaders)
						.ifPresent(yamlContractMessageHeaders -> yamlContractMessageHeaders.forEach((key, value) -> {
							YamlContract.KeyValueMatcher matcher = Optional.ofNullable(yamlContractInput.matchers)
									.map(yamlContractInputMatchers -> yamlContractInputMatchers.headers)
									.flatMap(yamlContractInputMatchersHeaders -> yamlContractInputMatchersHeaders
											.stream()
											.filter(yamlContractInputMatchersHeader -> yamlContractInputMatchersHeader.key
													.equals(key))
											.findFirst())
									.orElse(null);
							dslContractMessageHeaders.header(key, clientValue(value, matcher, key));
						})));
	}

	private void mapInputMessageBody(YamlContract.Input yamlContractInput, Input dslContractInput) {
		if (yamlContractInput.messageBody != null) {
			dslContractInput.messageBody(yamlContractInput.messageBody);
		}
		if (yamlContractInput.messageBodyFromFile != null) {
			dslContractInput.messageBody(file(yamlContractInput.messageBodyFromFile));
		}
		if (yamlContractInput.messageBodyFromFileAsBytes != null) {
			dslContractInput.messageBody(dslContractInput.fileAsBytes(yamlContractInput.messageBodyFromFileAsBytes));
		}
	}

	private void mapInputBodyMatchers(YamlContract.Input yamlContractInput, Input dslContractInput) {
		dslContractInput
				.bodyMatchers(dslContractInputBodyMatchers -> Optional.ofNullable(yamlContractInput.matchers.body)
						.ifPresent(yamlContractBodyStubMatchers -> yamlContractBodyStubMatchers
								.forEach(yamlContractBodyStubMatcher -> {
									ContentType contentType = evaluateClientSideContentType(
											yamlHeadersToContractHeaders(
													Optional.ofNullable(yamlContractInput.messageHeaders)
															.orElse(new HashMap<>())),
											Optional.ofNullable(yamlContractInput.messageBody).orElse(null));
									MatchingTypeValue value;
									switch (yamlContractBodyStubMatcher.type) {
									case by_date:
										value = dslContractInputBodyMatchers.byDate();
										break;
									case by_time:
										value = dslContractInputBodyMatchers.byTime();
										break;
									case by_timestamp:
										value = dslContractInputBodyMatchers.byTimestamp();
										break;
									case by_regex:
										String regex = yamlContractBodyStubMatcher.value;
										if (yamlContractBodyStubMatcher.predefined != null) {
											regex = predefinedToPattern(yamlContractBodyStubMatcher.predefined)
													.pattern();
										}
										value = dslContractInputBodyMatchers.byRegex(regex);
										break;
									case by_equality:
										value = dslContractInputBodyMatchers.byEquality();
										break;
									default:
										throw new UnsupportedOperationException("The type " + "["
												+ yamlContractBodyStubMatcher.type + "] is unsupported. "
												+ "Hint: If you're using <predefined> remember to pass < type:by_regex > ");
									}
									if (XML == contentType) {
										dslContractInputBodyMatchers.xPath(yamlContractBodyStubMatcher.path, value);
									}
									else {
										dslContractInputBodyMatchers.jsonPath(yamlContractBodyStubMatcher.path, value);
									}
								})));
	}

	private Headers yamlHeadersToContractHeaders(Map<String, Object> headers) {
		Set<Header> convertedHeaders = headers.keySet().stream()
				.map(header -> Header.build(header, headers.get(header))).collect(toSet());
		Headers contractHeaders = new Headers();
		contractHeaders.headers(convertedHeaders);
		return contractHeaders;
	}

	protected DslProperty<?> urlValue(String url, YamlContract.KeyValueMatcher urlMatcher) {
		if (urlMatcher != null) {
			if (urlMatcher.command != null) {
				return new DslProperty<Object>(url, new ExecutionProperty(urlMatcher.command));
			}
			return new DslProperty<>(urlMatcher.regex != null ? Pattern.compile(urlMatcher.regex)
					: urlMatcher.predefined != null ? predefinedToPattern(urlMatcher.predefined) : url, url);
		}
		return new DslProperty<>(url);
	}

	protected List<YamlContract> convert(ObjectMapper mapper, Object o) {
		try {
			return Arrays.asList(mapper.convertValue(o, YamlContract[].class));
		}
		catch (IllegalArgumentException e) {
			return Collections.singletonList(mapper.convertValue(o, YamlContract.class));
		}
	}

	protected Object serverValue(Object value, YamlContract.TestHeaderMatcher matcher, String key) {
		Object serverValue = value;
		if (matcher != null && matcher.regex != null) {
			serverValue = Pattern.compile(matcher.regex);
			Pattern pattern = (Pattern) serverValue;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.predefined != null) {
			Pattern pattern = predefinedToPattern(matcher.predefined);
			serverValue = pattern;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.command != null) {
			serverValue = new ExecutionProperty(matcher.command);
		}
		return serverValue;
	}

	protected DslProperty<?> serverCookieValue(Object value, YamlContract.TestCookieMatcher matcher, String key) {
		Object serverValue = value;
		if (matcher != null && matcher.regex != null) {
			serverValue = Pattern.compile(matcher.regex);
			Pattern pattern = (Pattern) serverValue;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.predefined != null) {
			Pattern pattern = predefinedToPattern(matcher.predefined);
			serverValue = pattern;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.command != null) {
			return new DslProperty<>(new ExecutionProperty(matcher.command), value);
		}
		return new DslProperty<>(value, serverValue);
	}

	protected DslProperty<?> clientValue(Object value, YamlContract.KeyValueMatcher matcher, String key) {
		Object clientValue = value instanceof DslProperty ? ((DslProperty<?>) value).getClientValue() : value;
		if (matcher != null && matcher.regex != null) {
			clientValue = Pattern.compile(matcher.regex);
			Pattern pattern = (Pattern) clientValue;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.predefined != null) {
			Pattern pattern = predefinedToPattern(matcher.predefined);
			clientValue = pattern;
			assertPatternMatched(pattern, value, key);
		}
		else if (matcher != null && matcher.command != null) {
			return new DslProperty<>(value, new ExecutionProperty(matcher.command));
		}
		return new DslProperty<>(clientValue, value);
	}

	protected Object queryParamValue(YamlContract yamlContract, String key, Object value) {
		Request request = new Request();
		YamlContract.QueryParameterMatcher matcher = yamlContract.request.matchers.queryParameters.stream()
				.filter(queryParameter -> queryParameter.key.equals(key)).findFirst().orElse(null);
		if (matcher == null) {
			return value;
		}
		switch (matcher.type) {
		case equal_to:
			return new DslProperty<>(request.equalTo(matcher.value), value);
		case containing:
			return new DslProperty<>(request.containing(matcher.value), value);
		case matching:
			return new DslProperty<>(request.matching(matcher.value), value);
		case not_matching:
			return new DslProperty<>(request.notMatching(matcher.value), value);
		case equal_to_json:
			return new DslProperty<>(request.equalToJson(matcher.value), value);
		case equal_to_xml:
			return new DslProperty<>(request.equalToXml(matcher.value), value);
		case absent:
			return new DslProperty<Object>(request.absent(), null);
		default:
			throw new UnsupportedOperationException("The provided matching type [" + matcher
					+ "] is unsupported. Use on of " + Arrays.toString(YamlContract.MatchingType.values()));
		}
	}

	protected Object serverValue(Object value, YamlContract.KeyValueMatcher matcher) {
		if (matcher != null && matcher.command != null) {
			return new ExecutionProperty(matcher.command);
		}
		return value instanceof DslProperty ? ((DslProperty<?>) value).getServerValue() : value;
	}

	private void assertPatternMatched(Pattern pattern, Object value, String key) {
		boolean matches = pattern.matcher(value.toString()).matches();
		if (!matches) {
			throw new IllegalStateException("Broken headers! A header with " + "key [" + key + "] with value [" + value
					+ "] is not matched by regex [" + pattern.pattern() + "]");
		}
	}

	protected Pattern predefinedToPattern(YamlContract.PredefinedRegex predefinedRegex) {
		switch (predefinedRegex) {
		case only_alpha_unicode:
			return RegexPatterns.onlyAlphaUnicode().getPattern();
		case number:
			return RegexPatterns.number().getPattern();
		case any_double:
			return RegexPatterns.aDouble().getPattern();
		case any_boolean:
			return RegexPatterns.anyBoolean().getPattern();
		case ip_address:
			return RegexPatterns.ipAddress().getPattern();
		case hostname:
			return RegexPatterns.hostname().getPattern();
		case email:
			return RegexPatterns.email().getPattern();
		case url:
			return RegexPatterns.url().getPattern();
		case uuid:
			return RegexPatterns.uuid().getPattern();
		case iso_date:
			return RegexPatterns.isoDate().getPattern();
		case iso_date_time:
			return RegexPatterns.isoDateTime().getPattern();
		case iso_time:
			return RegexPatterns.isoTime().getPattern();
		case iso_8601_with_offset:
			return RegexPatterns.iso8601WithOffset().getPattern();
		case non_empty:
			return RegexPatterns.nonEmpty().getPattern();
		case non_blank:
			return RegexPatterns.nonBlank().getPattern();
		default:
			throw new UnsupportedOperationException("The predefined regex [" + predefinedRegex
					+ "] is unsupported. Use one of " + Arrays.toString(YamlContract.PredefinedRegex.values()));
		}
	}

	protected String file(String relativePath) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(relativePath);
		if (resource == null) {
			throw new IllegalStateException("File [\"+relativePath+\"] is not present");
		}
		try {
			return String.join("\n", Files.readAllLines(Paths.get(resource.toURI())));
		}
		catch (URISyntaxException | IOException e) {
			throw new IllegalStateException("File [" + relativePath + "] syntax is incorrect");
		}
	}

	protected static ClassLoader updatedClassLoader(File rootFolder, ClassLoader classLoader) {
		try {
			ClassLoader urlCl = URLClassLoader.newInstance(new URL[] { rootFolder.toURI().toURL() }, classLoader);
			Thread.currentThread().setContextClassLoader(urlCl);
			return urlCl;
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("Root folder [" + rootFolder + "] URL is incorrect");
		}
	}

}
