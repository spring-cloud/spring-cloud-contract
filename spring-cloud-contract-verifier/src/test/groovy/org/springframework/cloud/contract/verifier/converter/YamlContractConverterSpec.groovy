package org.springframework.cloud.contract.verifier.converter

import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class YamlContractConverterSpec extends Specification {

	URL ymlUrl = YamlContractConverterSpec.getResource("/contract.yml")
	File yml = new File(ymlUrl.toURI())
	YamlContractConverter converter = new YamlContractConverter()

	def "should convert YAML to DSL"() {
		given:
			assert converter.isAccepted(yml)
		when:
			Contract contract = converter.convertFrom(yml)
		then:
			contract.request.url.clientValue == "/foo"
			contract.request.method.clientValue == "PUT"
			contract.request.headers.entries.find { it.name == "foo" && it.clientValue == "bar" }
			contract.request.body.clientValue == [foo: "bar"]
			contract.response.status.clientValue == 200
			contract.response.headers.entries.find { it.name == "foo2" && it.clientValue == "bar" }
			contract.response.body.clientValue == [foo2: "bar"]
	}

	def "should convert DSL to YAML"() {
		given:
			assert converter.isAccepted(yml)
		and:
			Contract contract = Contract.make {
				request {
					url("/foo")
					method("PUT")
					headers {
						header("foo", "bar")
					}
					body([foo: "bar"])
				}
				response {
					status(200)
					headers {
						header("foo2", "bar")
					}
					body([foo2: "bar"])
				}
			}
		when:
			YamlContract yamlContract = converter.convertTo(contract)
		then:
			yamlContract.request.url == "/foo"
			yamlContract.request.method == "PUT"
			yamlContract.request.headers.find { it.key == "foo" && it.value == "bar" }
			yamlContract.request.body == [foo: "bar"]
			yamlContract.response.status == 200
			yamlContract.response.headers.find { it.key == "foo2" && it.value == "bar" }
			yamlContract.response.body == [foo2: "bar"]
	}
}
