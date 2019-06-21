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

import java.util.regex.Pattern

import groovy.transform.PackageScope
import groovy.transform.TypeChecked

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.FromFileProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.MapConverter

/**
 * Knows how to build a Spock test methodBuilder for JaxRs.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 *
 * @since 1.0.0
 */
@PackageScope
@TypeChecked
class JaxRsClientSpockMethodRequestProcessingBodyBuilder extends SpockMethodRequestProcessingBodyBuilder {

	JaxRsClientSpockMethodRequestProcessingBodyBuilder(Contract stubDefinition,
			ContractVerifierConfigProperties configProperties,
			GeneratedClassDataForMethod classDataForMethod) {
		super(stubDefinition, configProperties, classDataForMethod)
	}

	@Override
	protected void given(BlockBuilder bb) {}

	@Override
	protected void givenBlock(BlockBuilder bb) {
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine("def response = webTarget")
		bb.indent()

		appendUrlPathAndQueryParameters(bb)
		appendRequestWithRequiredResponseContentType(bb)
		appendHeaders(bb)
		appendCookies(bb)
		appendMethodAndBody(bb)

		bb.unindent()

		bb.addEmptyLine()
		if (expectsResponseBody()) {
			bb.addLine("String responseAsString = response.readEntity(String)")
		}
	}

	protected void appendRequestWithRequiredResponseContentType(BlockBuilder bb) {
		String acceptHeader = getHeader("Accept")
		if (acceptHeader) {
			bb.addLine(".request('$acceptHeader')")
		}
		else {
			bb.addLine(".request()")
		}
	}

	protected void appendUrlPathAndQueryParameters(BlockBuilder bb) {
		if (request.url) {
			bb.addLine(".path(${concreteUrl(request.url)})")
			appendQueryParams(request.url.queryParameters, bb)
		}
		else if (request.urlPath) {
			bb.addLine(".path(${concreteUrl(request.urlPath)})")
			appendQueryParams(request.urlPath.queryParameters, bb)
		}
	}

	protected String concreteUrl(DslProperty url) {
		Object testSideUrl = MapConverter.getTestSideValues(url)
		if (!(testSideUrl instanceof ExecutionProperty)) {
			return "'" + testSideUrl.toString() + "'"
		}
		return testSideUrl.toString()
	}

	private void appendQueryParams(QueryParameters queryParameters, BlockBuilder bb) {
		if (!queryParameters?.parameters) {
			return
		}
		queryParameters.parameters.findAll(this.&allowedQueryParameter).
				each { QueryParameter param ->
					bb.addLine(".queryParam('$param.name', '${resolveParamValue(param).toString()}')")
				}
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, FromFileProperty value) {
		if (value.isByte()) {
			return "response.readEntity(byte[]) == " +
					readBytesFromFileString(value, CommunicationType.RESPONSE)
		}
		return getResponseBodyPropertyComparisonString(property, value.asString())
	}

	protected void appendMethodAndBody(BlockBuilder bb) {
		String method = request.method.serverValue.toString().toLowerCase()
		if (request.body) {
			String contentType =
					getHeader('Content-Type') ?: getRequestContentType().mimeType
			Object body = request.body.serverValue
			String value
			if (body instanceof ExecutionProperty) {
				value = body.toString()
			}
			else if (body instanceof FromFileProperty) {
				FromFileProperty fileProperty = (FromFileProperty) body
				value = fileProperty.isByte() ?
						readBytesFromFileString(fileProperty, CommunicationType.REQUEST) :
						readStringFromFileString(fileProperty, CommunicationType.REQUEST)
			}
			else {
				value = "'${bodyAsString}'"
			}
			bb.addLine(".methodBuilder('${method.toUpperCase()}', entity(${value}, '$contentType'))")
		}
		else {
			bb.addLine(".methodBuilder('${method.toUpperCase()}')")
		}
	}

	protected appendHeaders(BlockBuilder bb) {
		request.headers?.executeForEachHeader { Header header ->
			if (headerOfAbsentType(header)) {
				return
			}
			if (header.name == 'Content-Type' || header.name == 'Accept') {
				return
			} // Particular headers are set via 'request' / 'entity' methods
			bb.addLine(".header('${header.name}', '${header.serverValue}')")
		}
	}

	protected appendCookies(BlockBuilder bb) {
		request.cookies?.executeForEachCookie { Cookie cookie ->
			if (cookieOfAbsentType(cookie)) {
				return
			}

			bb.addLine(".cookie('${cookie.key}', '${cookie.serverValue}')")
		}
	}

	protected String getHeader(String name) {
		return request.headers?.entries?.find { it.name == name }?.serverValue
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("response.status == $response.status.serverValue")
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.executeForEachHeader { Header header ->
			processHeaderElement(bb, header.name, header.serverValue instanceof NotToEscapePattern ?
					header.serverValue :
					MapConverter.getTestSideValues(header.serverValue))
		}
	}

	@Override
	protected void validateResponseCookiesBlock(BlockBuilder bb) {
		response.cookies?.executeForEachCookie { Cookie cookie ->
			processCookieElement(bb, cookie.key, cookie.serverValue instanceof NotToEscapePattern ?
					cookie.serverValue :
					MapConverter.getTestSideValues(cookie.serverValue))
		}
	}

	@Override
	protected String getResponseAsString() {
		return 'responseAsString'
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			blockBuilder.
					addLine("response.getHeaderString('$property') ${convertHeaderComparison(((NotToEscapePattern) value).serverValue)}")
		}
		else {
			// fallback
			processHeaderElement(blockBuilder, property, value.toString())
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.
				addLine("${exec.insertValue("response.getHeaderString(\'$property\')")}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.
				addLine("response.getHeaderString('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Number value) {
		blockBuilder.addLine("response.getHeaderString('$property') == ${value}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern value) {
		blockBuilder.
				addLine("response.getHeaderString('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, Pattern pattern) {
		blockBuilder.addLine("response.getCookies().get('$key') != null")
		blockBuilder.
				addLine("response.getCookies().get('$key').getValue() ${convertCookieComparison(pattern)}")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, String value) {
		blockBuilder.addLine("response.getCookies().get('$key') != null")
		blockBuilder.
				addLine("response.getCookies().get('$key').getValue() ${convertCookieComparison(value)}")
	}

	@Override
	protected String postProcessJsonPathCall(String jsonPath) {
		if (templateProcessor.containsTemplateEntry(jsonPath)) {
			return jsonPath
		}
		return jsonPath.replace('$', '\\$')
	}
}
