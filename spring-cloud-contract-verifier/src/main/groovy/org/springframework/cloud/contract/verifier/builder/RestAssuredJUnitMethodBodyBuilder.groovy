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
import org.springframework.cloud.contract.spec.internal.ExecutionProperty
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.NotToEscapePattern
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties
import org.springframework.cloud.contract.verifier.util.MapConverter

import java.util.regex.Pattern
/**
 * A {@link JUnitMethodBodyBuilder} implementation that uses Rest Assured.
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.3
 */
@TypeChecked
@PackageScope
class RestAssuredJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	RestAssuredJUnitMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties) {
		super(stubDefinition, configProperties)
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertThat(response.statusCode()).isEqualTo($response.status.serverValue);")
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
	protected String getResponseBodyPropertyComparisonString(String property, Object value) {
		return null
	}

	@Override
	protected String getResponseBodyPropertyComparisonString(String property, Pattern value) {
		return """assertThat(responseBody).${createHeaderComparison(value)}"""
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Object value) {
		if (value instanceof NotToEscapePattern) {
			blockBuilder.addLine("assertThat(response.header(\"$property\"))." +
					"${createMatchesMethod((value as NotToEscapePattern).serverValue.pattern().replace("\\", "\\\\"))};")
		} else {
			// fallback
			processHeaderElement(blockBuilder, property, value.toString())
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Number value) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).isEqualTo(${value});")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.header(\"$property\")")};")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.getCookie(\"$key\")).isNotNull();")
		blockBuilder.addLine("assertThat(response.getCookie(\"$key\")).${createCookieComparison(pattern)}")
	}

	@Override
	protected void processCookieElement(BlockBuilder blockBuilder, String key, String value) {
		blockBuilder.addLine("assertThat(response.getCookie(\"$key\")).isNotNull();")
		blockBuilder.addLine("assertThat(response.getCookie(\"$key\")).${createCookieComparison(value)}")
	}
}
