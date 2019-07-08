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

package org.springframework.cloud.contract.verifier.converter

import spock.lang.Specification

class YamlContractSpec extends Specification {

	def "should convert to matching type from string"() {
		when:
			YamlContract.MatchingType type = YamlContract.MatchingType.from(string)
		then:
			type == expectedType
		where:
			string        || expectedType
			"equalTo"     || YamlContract.MatchingType.equal_to
			"equalToJson" || YamlContract.MatchingType.equal_to_json
			"containing"  || YamlContract.MatchingType.containing
			"unknown"     || null
	}
}

