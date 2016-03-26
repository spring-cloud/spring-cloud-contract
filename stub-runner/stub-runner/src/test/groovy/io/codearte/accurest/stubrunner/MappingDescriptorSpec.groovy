package io.codearte.accurest.stubrunner

import com.github.tomakehurst.wiremock.http.RequestMethod
import spock.lang.Specification

class MappingDescriptorSpec extends Specification {
	public static
	final File MAPPING_DESCRIPTOR = new File('src/test/resources/repository/mappings/com/ofg/ping/ping.json')

	def 'should describe stub mapping'() {
		given:
		MappingDescriptor mappingDescriptor = new MappingDescriptor(MAPPING_DESCRIPTOR)

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
