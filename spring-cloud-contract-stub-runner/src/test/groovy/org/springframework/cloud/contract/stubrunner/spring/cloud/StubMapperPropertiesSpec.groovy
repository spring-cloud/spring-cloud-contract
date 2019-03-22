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

package org.springframework.cloud.contract.stubrunner.spring.cloud

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class StubMapperPropertiesSpec extends Specification {

	def "should convert ivy notation to serviceId by fallbacking to artifactId if nothing else matches"() {
		given:
			Map<String, String> idsToServiceIds = [
					fraudDetectionServer: 'someNameThatShouldMapFraudDetectionServer'
			]
			StubMapperProperties properties = new StubMapperProperties(idsToServiceIds: idsToServiceIds)
		expect:
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('fraudDetectionServer')
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('groupid:fraudDetectionServer')
			'someNameThatShouldMapFraudDetectionServer' == properties.fromIvyNotationToId('groupid:fraudDetectionServer:+:classifier')
	}
}
