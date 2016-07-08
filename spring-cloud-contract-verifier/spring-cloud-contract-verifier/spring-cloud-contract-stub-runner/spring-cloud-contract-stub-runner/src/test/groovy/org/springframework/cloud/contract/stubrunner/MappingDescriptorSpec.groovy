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

package org.springframework.cloud.contract.stubrunner

import com.github.tomakehurst.wiremock.http.RequestMethod
import spock.lang.Specification

class MappingDescriptorSpec extends Specification {
	public static
	final File MAPPING_DESCRIPTOR = new File('src/test/resources/repository/mappings/spring/cloud/ping/ping.json')

	def 'should describe stub mapping'() {
		given:
		WiremockMappingDescriptor mappingDescriptor = new WiremockMappingDescriptor(MAPPING_DESCRIPTOR)

		expect:
		with(mappingDescriptor.mapping) {
			request.method == RequestMethod.GET
			request.url == '/ping'
			response.status == 200
			response.body == 'pong'
			response.headers.contentTypeHeader.mimeTypePart() == 'text/plain'
		}
	}
}
