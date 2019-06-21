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

package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.ContentUtils
import org.springframework.cloud.contract.verifier.util.MapConverter

import static org.springframework.cloud.contract.verifier.util.ContentUtils.evaluateContentType

/**
 * An abstraction for creating a test methodBuilder that includes processing of an HTTP request
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 * @author Tim Ysewyn
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
abstract class RequestProcessingMethodBodyBuilder extends MethodBodyBuilder {

	protected final Request request
	protected final Response response
	private static final String DOUBLE_QUOTE = '"'
	private static final String QUERY_PARAM_METHOD = 'queryParam'

	RequestProcessingMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties, GeneratedClassDataForMethod classDataForMethod) {
		super(configProperties, stubDefinition, classDataForMethod)
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	protected String escapeRequestSpecialChars(String string) {
		if (getRequestContentType() == ContentType.JSON) {
			return string
					.replaceAll('\\\\n', '\\\\\\\\n')
		}
		return string
	}

	/**
	 * @return code used to retrieve a response for the given {@link Request}
	 */
	protected abstract String getInputString(Request request)

	@Override
	protected boolean hasGivenSection() {
		return true
	}

	/**
	 * @return {@code true} if a response body is expected
	 */
	protected boolean expectsResponseBody() {
		return response.body != null
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(Object o) {
		return true
	}

	@Override
	protected void processInput(BlockBuilder bb) {
		request.headers?.executeForEachHeader { Header header ->
			if (headerOfAbsentType(header)) {
				return
			}
			bb.addLine(getHeaderString(header))
		}

		request.cookies?.executeForEachCookie { Cookie cookie ->
			if (cookieOfAbsentType(cookie)) {
				return
			}
			bb.addLine(getCookieString(cookie))
		}

		if (request.body) {
			Object body = null
			switch (request.body.serverValue) {
			case ExecutionProperty:
			case FromFileProperty:
				body = request.body?.serverValue
				break
			default:
				body = getBodyAsString()
			}
			bb.addLine(getBodyString(body))
		}
		if (request.multipart) {
			multipartParameters?.each { Map.Entry<String, Object> entry ->
				bb.
						addLine(getMultipartParameterLine(entry))
			}
		}
	}

	protected boolean headerOfAbsentType(Header header) {
		return header.serverValue instanceof MatchingStrategy &&
				((MatchingStrategy) header.serverValue).type == MatchingStrategy.Type.ABSENT
	}

	protected boolean cookieOfAbsentType(Cookie cookie) {
		return cookie.serverValue instanceof MatchingStrategy &&
				((MatchingStrategy) cookie.serverValue).type == MatchingStrategy.Type.ABSENT
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine(getInputString(request))
		bb.indent()

		Url url = getUrl(request)
		addQueryParameters(url, bb)
		addAsyncIfRequired(bb)
		addUrl(url, bb)
		addColonIfRequired(bb)
		bb.unindent()
	}

	private void addAsyncIfRequired(BlockBuilder bb) {
		if (response.async) {
			bb.addLine('.when().async()')
		}
		if (response.delay) {
			bb.addLine(".timeout(${response.delay.serverValue})")
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected addQueryParameters(Url buildUrl, BlockBuilder bb) {
		if (hasQueryParams(buildUrl)) {
			List<QueryParameter> queryParameters = buildUrl.queryParameters.parameters.
					findAll(this.&allowedQueryParameter)
			for (queryParam in queryParameters) {
				addQueryParameter(queryParam, bb)
			}
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected addQueryParameter(QueryParameter queryParam, BlockBuilder bb) {
		bb.addLine(".${QUERY_PARAM_METHOD}(${DOUBLE_QUOTE}${queryParam.name}"
				+
				"${DOUBLE_QUOTE},${DOUBLE_QUOTE}${resolveParamValue(queryParam).toString()}${DOUBLE_QUOTE})")
	}

	protected addUrl(Url buildUrl, BlockBuilder bb) {
		Object testSideUrl = MapConverter.getTestSideValues(buildUrl)
		String method = request.method.serverValue.toString().toLowerCase()
		String url = testSideUrl.toString()
		if (!(testSideUrl instanceof ExecutionProperty)) {
			url = "${DOUBLE_QUOTE}${testSideUrl.toString()}${DOUBLE_QUOTE}"
		}
		bb.addLine(/.${method}(${url})/)
	}

	@Override
	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.cookies) {
			validateResponseCookiesBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
			bb.addLine(addCommentSignIfRequired('and:')).startBlock()
			validateResponseBodyBlock(bb, response.bodyMatchers, response.body.serverValue)
		}
	}

	@Override
	protected void validateResponseBodyBlock(BlockBuilder bb, BodyMatchers bodyMatchers, Object responseBody) {
		super.validateResponseBodyBlock(bb, bodyMatchers, responseBody)
		String newBody = this.templateProcessor.transform(request, bb.toString())
		bb.updateContents(newBody)
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, GString value) {
		String gstringValue = ContentUtils.
				extractValueForGString(value, ContentUtils.GET_TEST_SIDE).toString()
		processHeaderElement(blockBuilder, property, gstringValue)
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, GString value) {
		String gStringValue = ContentUtils.
				extractValueForGString(value, ContentUtils.GET_TEST_SIDE).toString()
		processCookieElement(blockBuilder, key, gStringValue)
	}

	@Override
	protected ContentType getResponseContentType() {
		return evaluateContentType(response?.headers, response?.body?.serverValue)
	}

	@Override
	protected String getBodyAsString() {
		ContentType contentType = contentType()
		Object bodyValue =
				extractServerValueFromBody(contentType, request.body.serverValue)
		if (contentType == ContentType.FORM) {
			if (bodyValue instanceof Map) {
				// [a:3, b:4] == "a=3&b=4"
				return ((Map) bodyValue).collect {
					convertUnicodeEscapesIfRequired(it.key.toString() + "=" + it.value)
				}.join("&")
			}
			else if (bodyValue instanceof List) {
				// ["a=3", "b=4"] == "a=3&b=4"
				return ((List) bodyValue).collect {
					convertUnicodeEscapesIfRequired(it.toString())
				}.join("&")
			}
		}
		else {
			String json = new JsonOutput().toJson(bodyValue)
			json = convertUnicodeEscapesIfRequired(json)
			return trimRepeatedQuotes(json)
		}
	}

	/**
	 * @return a map of server side multipart parameters
	 */
	protected Map<String, Object> getMultipartParameters() {
		return (Map<String, Object>) request?.multipart?.serverValue
	}

	/**
	 * Maps the {@link Request} into a {@link ContentType}
	 */
	protected ContentType getRequestContentType() {
		return evaluateContentType(request?.headers, request?.body?.serverValue)
	}

	/**
	 * @return a String URL from {@link Request}'s test side values. It can be
	 * a concrete value of the URL or a path.
	 */
	protected Url getUrl(Request request) {
		if (request.url) {
			return request.url
		}
		if (request.urlPath) {
			return request.urlPath
		}
		throw new IllegalStateException("URL is not set!")
	}

	/**
	 * @return a line of code to send a multi part parameter in the request
	 */
	protected String getMultipartParameterLine(Map.Entry<String, Object> parameter) {
		if (parameter.value instanceof NamedProperty) {
			return ".multiPart(${getMultipartFileParameterContent(parameter.key, (NamedProperty) parameter.value)})"
		}
		return getParameterString(parameter)
	}

	private boolean hasQueryParams(Url url) {
		return url.queryParameters
	}
}
