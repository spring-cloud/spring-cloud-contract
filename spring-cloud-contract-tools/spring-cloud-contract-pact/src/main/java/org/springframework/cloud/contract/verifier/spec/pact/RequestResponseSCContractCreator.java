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

package org.springframework.cloud.contract.verifier.spec.pact;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.OptionalBody;
import au.com.dius.pact.core.model.ProviderState;
import au.com.dius.pact.core.model.Request;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.Response;
import au.com.dius.pact.core.model.matchingrules.Category;
import au.com.dius.pact.core.model.matchingrules.DateMatcher;
import au.com.dius.pact.core.model.matchingrules.MatchingRule;
import au.com.dius.pact.core.model.matchingrules.MatchingRuleGroup;
import au.com.dius.pact.core.model.matchingrules.MaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinMaxTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.MinTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.NullMatcher;
import au.com.dius.pact.core.model.matchingrules.NumberTypeMatcher;
import au.com.dius.pact.core.model.matchingrules.RegexMatcher;
import au.com.dius.pact.core.model.matchingrules.RuleLogic;
import au.com.dius.pact.core.model.matchingrules.TimeMatcher;
import au.com.dius.pact.core.model.matchingrules.TimestampMatcher;
import au.com.dius.pact.core.model.matchingrules.TypeMatcher;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.RegexPatterns;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;

/**
 * Creator of {@link Contract} instances.
 *
 * @author Tim Ysewyn
 * @author Stessy Delcroix
 * @since 2.0.0
 */
class RequestResponseSCContractCreator {

	private static final String FULL_BODY = "$";

	Collection<Contract> convertFrom(RequestResponsePact pact) {
		return pact.getInteractions().stream().map(interaction -> Contract.make(contract -> {
			mapContractDescription(interaction, contract);
			mapContractRequest(interaction, contract);
			mapContractResponse(interaction, contract);
		})).collect(Collectors.toList());
	}

	private void mapContractDescription(Interaction interaction, Contract contract) {
		contract.description(buildDescription(interaction));
	}

	private void mapContractRequest(Interaction inter, Contract contract) {
		contract.request((contractRequest) -> {
			if (!(inter instanceof RequestResponseInteraction)) {
				return;
			}
			RequestResponseInteraction interaction = (RequestResponseInteraction) inter;
			Request pactRequest = interaction.getRequest();
			contractRequest.method(pactRequest.getMethod());
			mapRequestUrl(contractRequest, pactRequest);

			if (!pactRequest.getHeaders().isEmpty()) {
				mapRequestHeaders(contractRequest, pactRequest);
			}
			if (pactRequest.getHeaders().containsKey("Cookie")) {
				mapRequestCookies(contractRequest, pactRequest);
			}
			if (pactRequest.getBody().getState() == OptionalBody.State.PRESENT) {
				mapRequestBody(contractRequest, pactRequest);
			}
			Category bodyRules = pactRequest.getMatchingRules().rulesForCategory("body");
			if (!bodyRules.getMatchingRules().isEmpty()) {
				mapRequestBodyRules(contractRequest, bodyRules);
			}
		});
	}

	private void mapContractResponse(Interaction inter, Contract contract) {
		contract.response((contractResponse) -> {
			if (!(inter instanceof RequestResponseInteraction)) {
				return;
			}
			RequestResponseInteraction interaction = (RequestResponseInteraction) inter;
			Response pactResponse = interaction.getResponse();
			contractResponse.status(pactResponse.getStatus());
			if (pactResponse.getBody().isPresent()) {
				mapResponseBody(contractResponse, pactResponse);
			}

			Category bodyRules = pactResponse.getMatchingRules().rulesForCategory("body");
			if (!bodyRules.getMatchingRules().isEmpty()) {
				mapResponseBodyRules(contractResponse, pactResponse, bodyRules);
			}
			if (!pactResponse.getHeaders().isEmpty()) {
				mapResponseHeaders(contractResponse, pactResponse);
			}

			if (pactResponse.getHeaders().containsKey("Cookie")) {
				mapResponseCookies(contractResponse, pactResponse);
			}
		});
	}

	private void mapResponseBodyRules(org.springframework.cloud.contract.spec.internal.Response contractResponse,
			Response pactResponse, Category bodyRules) {
		contractResponse.bodyMatchers((bodyMatchers) -> {
			bodyRules.getMatchingRules().forEach((key, matchindRuleGroup) -> {
				if (matchindRuleGroup.getRuleLogic() != RuleLogic.AND) {
					throw new UnsupportedOperationException(
							"Currently only the AND combination rule logic is supported");
				}
				if (FULL_BODY.equals(key)) {
					JsonPaths jsonPaths = JsonToJsonPathsConverter
							.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(
									new String(pactResponse.getBody().getValue()));
					jsonPaths.forEach(
							(jsonPath) -> bodyMatchers.jsonPath(jsonPath.keyBeforeChecking(), bodyMatchers.byType()));
				}
				else {
					matchindRuleGroup.getRules().forEach((matchingRule) -> {
						if (matchingRule instanceof NullMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byNull());
						}
						else if (matchingRule instanceof RegexMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byRegex(((RegexMatcher) matchingRule).getRegex()));
						}
						else if (matchingRule instanceof DateMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byDate());
						}
						else if (matchingRule instanceof TimeMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byTime());
						}
						else if (matchingRule instanceof TimestampMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byTimestamp());
						}
						else if (matchingRule instanceof MinTypeMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byType((valueHolder) -> valueHolder
									.minOccurrence((((MinTypeMatcher) matchingRule).getMin()))));
						}
						else if (matchingRule instanceof MinMaxTypeMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byType((valueHolder) -> {
								valueHolder.minOccurrence((((MinMaxTypeMatcher) matchingRule).getMin()));
								valueHolder.maxOccurrence((((MinMaxTypeMatcher) matchingRule).getMax()));
							}));
						}
						else if (matchingRule instanceof MaxTypeMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byType((valueHolder) -> valueHolder
									.maxOccurrence((((MaxTypeMatcher) matchingRule).getMax()))));
						}
						else if (matchingRule instanceof TypeMatcher) {
							bodyMatchers.jsonPath(key, bodyMatchers.byType());
						}
						else if (matchingRule instanceof NumberTypeMatcher) {
							switch (((NumberTypeMatcher) matchingRule).getNumberType()) {
							case NUMBER:
								bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.number()));
								break;
							case INTEGER:
								bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.anInteger()));
								break;
							case DECIMAL:
								bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.aDouble()));
								break;
							default:
								throw new UnsupportedOperationException("Unsupported number type!");
							}
						}
					});
				}
			});
		});
	}

	private void mapResponseBody(org.springframework.cloud.contract.spec.internal.Response contractResponse,
			Response pactResponse) {
		Object parsedBody = BodyConverter.toSCCBody(pactResponse);
		if (parsedBody instanceof Map) {
			contractResponse.body((Map) parsedBody);
		}
		else if (parsedBody instanceof List) {
			contractResponse.body((List) parsedBody);
		}
		else {
			contractResponse.body(parsedBody.toString());
		}
	}

	private void mapRequestBodyRules(org.springframework.cloud.contract.spec.internal.Request contractRequest,
			Category bodyRules) {
		contractRequest.bodyMatchers((bodyMatchers) -> {
			bodyRules.getMatchingRules().forEach((key, matchingRuleGroup) -> {
				if (matchingRuleGroup.getRuleLogic() != RuleLogic.AND) {
					throw new UnsupportedOperationException(
							"Currently only the AND combination rule logic is supported");
				}

				matchingRuleGroup.getRules().forEach((matchingRule) -> {
					if (matchingRule instanceof RegexMatcher) {
						bodyMatchers.jsonPath(key, bodyMatchers.byRegex(((RegexMatcher) matchingRule).getRegex()));
					}
					else if (matchingRule instanceof DateMatcher) {
						bodyMatchers.jsonPath(key, bodyMatchers.byDate());
					}
					else if (matchingRule instanceof TimeMatcher) {
						bodyMatchers.jsonPath(key, bodyMatchers.byTime());
					}
					else if (matchingRule instanceof TimestampMatcher) {
						bodyMatchers.jsonPath(key, bodyMatchers.byTimestamp());
					}
					else if (matchingRule instanceof NumberTypeMatcher) {
						switch (((NumberTypeMatcher) matchingRule).getNumberType()) {
						case NUMBER:
							bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.number()));
							break;
						case INTEGER:
							bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.anInteger()));
							break;
						case DECIMAL:
							bodyMatchers.jsonPath(key, bodyMatchers.byRegex(RegexPatterns.aDouble()));
							break;
						default:
							throw new RuntimeException("Unsupported number type!");
						}
					}
				});
			});
		});
	}

	private void mapRequestBody(org.springframework.cloud.contract.spec.internal.Request contractRequest,
			Request pactRequest) {
		Object parsedBody = BodyConverter.toSCCBody(pactRequest);
		if (parsedBody instanceof Map) {
			contractRequest.body((Map) parsedBody);
		}
		else if (parsedBody instanceof List) {
			contractRequest.body((List) parsedBody);
		}
		else {
			contractRequest.body(parsedBody.toString());
		}
	}

	private void mapRequestCookies(org.springframework.cloud.contract.spec.internal.Request contractRequest,
			Request pactRequest) {
		Category headerRules = pactRequest.getMatchingRules().rulesForCategory("header");
		String[] splitCookiesHeader = pactRequest.getHeaders().get("Cookie").get(0).split(";");
		Map<String, String> foundCookies = Stream.of(splitCookiesHeader).map((cookieHeader) -> cookieHeader.split("="))
				.collect(Collectors.toMap(splittedCookieHeader -> splittedCookieHeader[0],
						splittedCookieHeader -> splittedCookieHeader[1]));

		contractRequest.cookies((cookies) -> foundCookies.forEach((key, value) -> {
			if (headerRules.getMatchingRules().containsKey("Cookie")) {
				MatchingRuleGroup matchingRuleGroup = headerRules.getMatchingRules().get("Cookie");
				if (matchingRuleGroup.getRules().size() > 1) {
					throw new UnsupportedOperationException(
							"Currently only 1 rule at a time for a header is supported");
				}
				MatchingRule matchingRule = matchingRuleGroup.getRules().get(0);
				if (matchingRule instanceof RegexMatcher) {
					cookies.cookie(key,
							contractRequest.$(
									contractRequest.c(contractRequest.regex(((RegexMatcher) matchingRule).getRegex())),
									contractRequest.p(value)));
				}
				else {
					throw new UnsupportedOperationException(
							"Currently only the header matcher of type regex is supported");
				}
			}
			else {
				cookies.cookie(key, value);
			}
		}));
	}

	private void mapResponseCookies(org.springframework.cloud.contract.spec.internal.Response contractResponse,
			Response pactResponse) {
		Category headerRules = pactResponse.getMatchingRules().rulesForCategory("header");
		String[] splitCookiesHeader = pactResponse.getHeaders().get("Cookie").get(0).split(";");
		Map<String, String> foundCookies = Stream.of(splitCookiesHeader).map((cookieHeader) -> cookieHeader.split("="))
				.collect(Collectors.toMap(splittedCookieHeader -> splittedCookieHeader[0],
						splittedCookieHeader -> splittedCookieHeader[1]));

		contractResponse.cookies((cookies) -> foundCookies.forEach((key, value) -> {
			if (headerRules.getMatchingRules().containsKey("Cookie")) {
				MatchingRuleGroup matchingRuleGroup = headerRules.getMatchingRules().get("Cookie");
				if (matchingRuleGroup.getRules().size() > 1) {
					throw new UnsupportedOperationException(
							"Currently only 1 rule at a time for a header is supported");
				}
				MatchingRule matchingRule = matchingRuleGroup.getRules().get(0);
				if (matchingRule instanceof RegexMatcher) {
					cookies.cookie(key, contractResponse.$(
							contractResponse.p(
									contractResponse.regex(Pattern.compile(((RegexMatcher) matchingRule).getRegex()))),
							contractResponse.c(value)));
				}
				else {
					throw new UnsupportedOperationException(
							"Currently only the header matcher of type regex is supported");
				}
			}
			else {
				cookies.cookie(key, value);
			}
		}));
	}

	private void mapRequestUrl(org.springframework.cloud.contract.spec.internal.Request contractRequest,
			Request pactRequest) {
		if (!pactRequest.getQuery().isEmpty()) {
			contractRequest.url(pactRequest.getPath(),
					(url) -> url.queryParameters((queryParameters) -> pactRequest.getQuery().forEach((key,
							values) -> values.forEach((singleValue) -> queryParameters.parameter(key, singleValue)))));
		}
		else {
			contractRequest.url(pactRequest.getPath());
		}
	}

	private void mapRequestHeaders(org.springframework.cloud.contract.spec.internal.Request contractRequest,
			Request pactRequest) {
		Category headerRules = pactRequest.getMatchingRules().rulesForCategory("header");
		contractRequest.headers((headers) -> pactRequest.getHeaders().forEach((key, values) -> {
			if (key.equalsIgnoreCase("Cookie")) {
				return;
			}
			if (headerRules.getMatchingRules().containsKey(key)) {
				MatchingRuleGroup matchingRuleGroup = headerRules.getMatchingRules().get(key);
				if (matchingRuleGroup.getRules().size() > 1) {
					throw new UnsupportedOperationException(
							"Currently only 1 rule at a time for a header is supported");
				}
				MatchingRule matchingRule = matchingRuleGroup.getRules().get(0);
				if (matchingRule instanceof RegexMatcher) {
					values.forEach((value) -> {
						headers.header(key,
								contractRequest.$(
										contractRequest
												.c(contractRequest.regex(((RegexMatcher) matchingRule).getRegex())),
										contractRequest.p(value)));
					});
				}
				else {
					throw new UnsupportedOperationException(
							"Currently only the header matcher of type regex is supported");
				}
			}
			else {
				values.forEach((value) -> headers.header(key, value));
			}
		}));
	}

	private void mapResponseHeaders(org.springframework.cloud.contract.spec.internal.Response contractResponse,
			Response pactResponse) {
		Category headerRules = pactResponse.getMatchingRules().rulesForCategory("header");
		contractResponse.headers((headers) -> pactResponse.getHeaders().forEach((key, values) -> {
			if (key.equalsIgnoreCase("Cookie")) {
				return;
			}
			if (headerRules.getMatchingRules().containsKey(key)) {
				MatchingRuleGroup matchingRuleGroup = headerRules.getMatchingRules().get(key);
				if (matchingRuleGroup.getRules().size() > 1) {
					throw new UnsupportedOperationException(
							"Currently only 1 rule at a time for a header is supported");
				}
				MatchingRule matchingRule = matchingRuleGroup.getRules().get(0);
				if (matchingRule instanceof RegexMatcher) {
					values.forEach((value) -> {
						headers.header(key,
								contractResponse.$(
										contractResponse.p(contractResponse
												.regex(Pattern.compile(((RegexMatcher) matchingRule).getRegex()))),
										contractResponse.c(value)));
					});
				}
				else {
					throw new UnsupportedOperationException(
							"Currently only the header matcher of type regex is supported");
				}
			}
			else {
				values.forEach((value) -> headers.header(key, value));
			}
		}));
	}

	private String buildDescription(Interaction interaction) {
		StringBuilder description = new StringBuilder(interaction.getDescription());
		List<ProviderState> providerStates = interaction.getProviderStates();
		for (ProviderState providerState : providerStates) {
			description.append(" ").append(providerState.getName());
			Map<String, Object> params = providerState.getParams();
			if (!params.isEmpty()) {
				description.append("(");
				params.forEach((k, v) -> {
					description.append(k).append(": ").append(v.toString()).append(", ");
				});
				description.delete(description.length() - 2, description.length());
				description.append(")");
			}
		}
		return description.toString();
	}

}
