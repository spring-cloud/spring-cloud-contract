/*
 *  Copyright 2013-2016 the original author or authors.
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
import org.springframework.cloud.contract.spec.internal.Header
import org.springframework.cloud.contract.spec.internal.ExecutionProperty

import java.util.regex.Pattern

/**
 * A {@link JUnitMethodBodyBuilder} implementation that uses MockMvc to send requests.
 *
 * @author Olga Maciaszek-Sharma, codearte.io
 *
 * @since 1.0.0
 */
@TypeChecked
@PackageScope
class MockMvcJUnitMethodBodyBuilder extends JUnitMethodBodyBuilder {

	MockMvcJUnitMethodBodyBuilder(Contract stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void validateResponseCodeBlock(BlockBuilder bb) {
		bb.addLine("assertThat(response.statusCode()).isEqualTo($response.status.serverValue);")
	}

	@Override
	protected void validateResponseHeadersBlock(BlockBuilder bb) {
		response.headers?.executeForEachHeader { Header header ->\
			processHeaderElement(bb, header.name, header.serverValue)
		}
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, String value) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(value)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, Pattern pattern) {
		blockBuilder.addLine("assertThat(response.header(\"$property\")).${createHeaderComparison(pattern)}")
	}

	@Override
	protected void processHeaderElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec) {
		blockBuilder.addLine("${exec.insertValue("response.header(\"$property\")")};")
	}

}
