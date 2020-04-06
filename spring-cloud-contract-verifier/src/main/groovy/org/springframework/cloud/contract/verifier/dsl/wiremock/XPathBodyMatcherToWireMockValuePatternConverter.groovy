/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.cloud.contract.verifier.dsl.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.internal.MatchingType

import static org.springframework.cloud.contract.spec.internal.MatchingType.EQUALITY

/**
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
@CompileStatic
@PackageScope
class XPathBodyMatcherToWireMockValuePatternConverter {

	static StringValuePattern mapToPattern(MatchingType type, String value) {
		switch (type) {
		case EQUALITY: return WireMock.equalTo(value)
		default: return WireMock.matching(value)
		}
	}

}
