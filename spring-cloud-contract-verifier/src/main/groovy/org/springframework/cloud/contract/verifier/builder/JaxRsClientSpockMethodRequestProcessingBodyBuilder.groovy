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
import org.springframework.cloud.contract.spec.internal.DslProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.MapConverter

import java.util.regex.Pattern

/**
 * Knows how to build a Spock test method for JaxRs.
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

	JaxRsClientSpockMethodRequestProcessingBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(stubDefinition, configProperties)
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
		appendMethodAndBody(bb)

		bb.unindent()

		bb.addEmptyLine()
		bb.addLine("String responseAsString = response.readEntity(String)")
	}

	protected void appendRequestWithRequiredResponseContentType(BlockBuilder bb) {
		String acceptHeader = getHeader("Accept")
		if (acceptHeader) {
			bb.addLine(".request('$acceptHeader')")
		} else {
			bb.addLine(".request()")
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
			return "'" + testSideUrl.toString() + "'"
		}
		return testSideUrl.toString()
	}

	private void appendQueryParams(QueryParameters queryParameters, BlockBuilder bb) {
		if (!queryParameters?.parameters) {
			return
		}
		queryParameters.parameters.findAll(this.&allowedQueryParameter).each { QueryParameter param ->
			bb.addLine(".queryParam('$param.name', '${resolveParamValue(param).toString()}')")
		}
	}

	protected void appendMethodAndBody(BlockBuilder bb) {
		String method = request.method.serverValue.toString().toLowerCase()
		if (request.body) {
			String contentType = getHeader('Content-Type') ?: getRequestContentType().mimeType
			String body = request.body?.serverValue instanceof ExecutionProperty ?
					request.body?.serverValue?.toString() : "'${bodyAsString}'"
			bb.addLine(".method('${method.toUpperCase()}', entity(${body}, '$contentType'))")
		} else {
			bb.addLine(".method('${method.toUpperCase()}')")
		}
	}

	protected appendHeaders(BlockBuilder bb) {
		request.headers?.executeForEachHeader { Header header ->
			if (headerOfAbsentType(header)) {
				return
			}
			if (header.name == 'Content-Type' || header.name == 'Accept') return // Particular headers are set via 'request' / 'entity' methods
			bb.addLine(".header('${header.name}', '${header.serverValue}')")
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
	protected String getResponseAsString() {
		return 'responseAsString'
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			blockBuilder.addLine("response.getHeaderString('$property') ${convertHeaderComparison(((NotToEscapePattern) value).serverValue)}")
		} else {
			// fallback
			processHeaderElement(blockBuilder, property, value.toString())
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.getHeaderString(\'$property\')")}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("response.getHeaderString('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Number value) {
		blockBuilder.addLine("response.getHeaderString('$property') == ${value}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern value) {
		blockBuilder.addLine("response.getHeaderString('$property') ${convertHeaderComparison(value)}")
	}

	@Override
	protected String postProcessJsonPathCall(String jsonPath) {
		if (templateProcessor.containsTemplateEntry(jsonPath)) {
			return jsonPath
		}
		return jsonPath.replace('$', '\\$')
	}
}
