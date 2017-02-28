/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.BodyMatchers
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.MatchingStrategy
import org.springframework.cloud.contract.spec.internal.NamedProperty
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.cloud.contract.spec.internal.Response
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.ContentType
import org.springframework.cloud.contract.verifier.util.MapConverter

import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromContent
import static org.springframework.cloud.contract.verifier.util.ContentUtils.recognizeContentTypeFromHeader
/**
 * An abstraction for creating a test method that includes processing of an HTTP request
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
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

	RequestProcessingMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(configProperties)
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	/**
	 * Returns code used to retrieve a response for the given {@link Request}
	 */
	protected abstract String getInputString(Request request)

	@Override
	protected boolean hasGivenSection() {
		return true
	}

	/**
	 * Returns {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	/**
	 * Returns {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	/**
	 * Returns {@code true} if the query parameter is allowed
	 */
	protected boolean allowedQueryParameter(Object o) {
		return true
	}

	@Override
	protected void processInput(BlockBuilder bb) {
		request.headers?.executeForEachHeader { Header header ->
			bb.addLine(getHeaderString(header))
		}
		if (request.body) {
			bb.addLine(getBodyString(bodyAsString))
		}
		if (request.multipart) {
			multipartParameters?.each { Map.Entry<String, Object> entry -> bb.addLine(getMultipartParameterLine(entry)) }
		}
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine(getInputString(request))
		bb.indent()

		Url url = getUrl(request)
		addQueryParameters(url, bb)
		addUrl(url, bb)
		addColonIfRequired(bb)
		bb.unindent()
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected addQueryParameters(Url buildUrl, BlockBuilder bb) {
		if(hasQueryParams(buildUrl)){
			List<QueryParameter> queryParameters = buildUrl.queryParameters.parameters.findAll(this.&allowedQueryParameter)
			for (queryParam in queryParameters) {
				addQueryParameter(queryParam, bb)
			}
		}
	}

	@TypeChecked(TypeCheckingMode.SKIP)
	protected addQueryParameter(QueryParameter queryParam, BlockBuilder bb) {
		bb.addLine(/.${QUERY_PARAM_METHOD}(${DOUBLE_QUOTE}${queryParam.name}${DOUBLE_QUOTE},${DOUBLE_QUOTE}${resolveParamValue(queryParam).toString()}${DOUBLE_QUOTE})/)
	}
	
	protected addUrl(Url buildUrl, BlockBuilder bb){
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
		if (response.body) {
			bb.endBlock()
			bb.addLine(addCommentSignIfRequired('and:')).startBlock()
			validateResponseBodyBlock(bb, response.matchers, response.body.serverValue)
		}
	}

	@Override
	protected void validateResponseBodyBlock(BlockBuilder bb, BodyMatchers bodyMatchers, Object responseBody) {
		super.validateResponseBodyBlock(bb, bodyMatchers, responseBody)
		String newBody = this.templateProcessor.transform(request, bb.toString())
		bb.updateContents(newBody)
	}

	@Override
	protected ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(response.body.serverValue)
		}
		return contentType
	}

	@Override
	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapesIfRequired(json)
		return trimRepeatedQuotes(json)
	}

	/**
	 * Returns a map of server side multipart parameters
	 */
	protected Map<String, Object> getMultipartParameters() {
		return (Map<String, Object>) request?.multipart?.serverValue
	}

	/**
	 * Maps the {@link Request} into a {@link ContentType}
	 */
	protected ContentType getRequestContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(request.body.serverValue)
		}
		return contentType
	}

	/**
	 * Returns a String URL from {@link Request}'s test side values. It can be
	 * a concrete value of the URL or a path.
	 */
	protected Url getUrl(Request request) {
		if (request.url)
			return request.url
		if (request.urlPath)
			return request.urlPath
		throw new IllegalStateException("URL is not set!")
	}

	/**
	 * Returns a line of code to send a multi part parameter in the request
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