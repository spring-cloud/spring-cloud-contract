package org.springframework.cloud.contract.verifier.builder

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.file.ContractMetadata
import spock.lang.Issue
import spock.lang.Specification

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
                    status 200
                    body(foo: "foo")
                    headers {
                        contentType(applicationJson())
                    }
                }
            }
            ContractMetadata metadata = new ContractMetadata(null, false, 0, null, contractDsl)
        when:
            File stubFile = new File("invalid-method:name.groovy")

            String methodName = MethodBuilder.methodName(metadata, stubFile, contractDsl)
        then:
            methodName == "invalid_method_name"
    }

}
