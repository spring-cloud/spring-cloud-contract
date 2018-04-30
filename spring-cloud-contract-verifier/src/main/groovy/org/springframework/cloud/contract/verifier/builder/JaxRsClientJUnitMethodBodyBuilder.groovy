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

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Cookie
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.MapConverter

import java.util.regex.Pattern

import static org.springframework.cloud.contract.verifier.config.TestFramework.JUNIT

/**
 * JaxRs implementation of the {@link JUnitMethodBodyBuilder}. Knows how to build
 * a test method for JaxRs.
 *
 * Do not change to {@code @CompileStatic} since it's using double dispatch.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class JaxRsClientJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	JaxRsClientJUnitMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(stubDefinition, configProperties)
	}

	@Override
	protected void given(BlockBuilder bb) {}

	@Override
	protected void givenBlock(BlockBuilder bb) {
	}

	@Override
	protected void when(BlockBuilder bb) {
		bb.addLine("Response response = webTarget")
		bb.indent()

		appendUrlPathAndQueryParameters(bb)
		appendRequestWithRequiredResponseContentType(bb)
		appendHeaders(bb)
		appendCookies(bb)
		appendMethodAndBody(bb)
		bb.addAtTheEnd(JUNIT.lineSuffix)

		bb.unindent()

		bb.addEmptyLine()
		if (expectsResponseBody()) {
			bb.addLine("String responseAsString = response.readEntity(String.class);")
		}
	}

	protected void appendUrlPathAndQueryParameters(BlockBuilder bb) {
		if (request.url) {
			bb.addLine(".path(${concreteUrl(request.url)})")
			appendQueryParams(request.url.queryParameters, bb)
		} else if (request.urlPath) {
			bb.addLine(".path(${concreteUrl(request.urlPath)})")
			appendQueryParams(request.urlPath.queryParameters, bb)
		}
	}

	protected String concreteUrl(DslProperty url) {
		Object testSideUrl = MapConverter.getTestSideValues(url)
		if (!(testSideUrl instanceof ExecutionProperty)) {
			return '"' + testSideUrl.toString() + '"'
		}
		return testSideUrl.toString()
	}

	private void appendQueryParams(QueryParameters queryParameters, BlockBuilder bb) {
		if (!queryParameters?.parameters) {
			return
		}
		queryParameters.parameters.findAll(this.&allowedQueryParameter).each { QueryParameter param ->
			bb.addLine(".queryParam(\"$param.name\", \"${resolveParamValue(param).toString()}\")")
		}
	}

	protected void appendMethodAndBody(BlockBuilder bb) {
		String method = request.method.serverValue.toString().toLowerCase()
		if (request.body) {
			String contentType = getHeader('Content-Type') ?: getRequestContentType().mimeType
			String body = request.body?.serverValue instanceof ExecutionProperty ?
					request.body?.serverValue?.toString() : "\"${bodyAsString}\""
			bb.addLine(".method(\"${method.toUpperCase()}\", entity(${body}, \"$contentType\"))")
		} else {
			bb.addLine(".method(\"${method.toUpperCase()}\")")
		}
	}

	protected appendHeaders(BlockBuilder bb) {
		request.headers?.executeForEachHeader { Header header ->
			if (headerOfAbsentType(header)) {
				return
			}
			if (header.name == 'Content-Type' || header.name == 'Accept') return
			bb.addLine(".header(\"${header.name}\", \"${header.serverValue}\")")
		}
	}

	protected appendCookies(BlockBuilder bb) {
		request.cookies?.executeForEachCookie { Cookie cookie ->
			if (cookieOfAbsentType(cookie)) {
				return
			}

			bb.addLine(".cookie(\"${cookie.key}\", \"${cookie.serverValue}\")")
		}
	}

	protected void appendRequestWithRequiredResponseContentType(BlockBuilder bb) {
		String acceptHeader = getHeader("Accept")
		if (acceptHeader) {
			bb.addLine(".request(\"$acceptHeader\")")
		} else {
			bb.addLine(".request()")
		}
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertThat(response.getStatus()).isEqualTo($response.status.serverValue);")
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

	protected String getHeader(String name) {
		return request.headers?.entries.find { it.name == name }?.serverValue
	}

	@Override
	protected String getResponseAsString() {
		return 'responseAsString'
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).${createHeaderComparison(((NotToEscapePattern) value).serverValue)}")
		} else {
			// fallback
			processHeaderElement(blockBuilder, property, value.toString())
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Number value) {
		blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).isEqualTo(${value});")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getHeaderString(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.getHeaderString(\"$property\")")};")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getCookies().get(\"$key\")).isNotNull();")
		blockBuilder.addLine("assertThat(response.getCookies().get(\"$key\").getValue()).${createCookieComparison(pattern)}")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, String value) {
		blockBuilder.addLine("assertThat(response.getCookies().get(\"$key\")).isNotNull();")
		blockBuilder.addLine("assertThat(response.getCookies().get(\"$key\").getValue()).${createCookieComparison(value)}")
	}

}
