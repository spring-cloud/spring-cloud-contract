package org.springframework.cloud.contract.verifier.builder


import spock.lang.Issue
import spock.lang.Specification

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata

class MethodBuilderSpec extends Specification {

	@Issue('#518')
	def "should map create valid method name from file name containing illegal chars"() {
		given:
			Contract contractDsl = Contract.make {
				request {
					method 'GET'
					urlPath '/foo'
				}
				response {
					status OK()
					body(foo: "foo")
					headers {
						contentType(applicationJson())
					}
				}
			}
			File stubFile = new File("invalid-method:name.groovy")

			ContractMetadata metadata = new ContractMetadata(stubFile.toPath(), false, 0, null, contractDsl)
			SingleContractMetadata singleContractMetadata = new SingleContractMetadata(contractDsl, metadata)
		when:

			String methodName = new NameProvider().methodName(singleContractMetadata)
		then:
			methodName == "validate_invalid_method_name"
	}

}
