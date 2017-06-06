package org.springframework.cloud.contract.verifier.spec.raml

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import spock.lang.Specification
import spock.lang.Subject
/**
 * @author Eddú Meléndez
 */
class RamlContractConverterSpec extends Specification {

    File ramlFile = new File(RamlContractConverterSpec.getResource("/api.raml").toURI())
    @Subject
    RamlContractConverter converter = new RamlContractConverter()

    def "should accept raml files"() {
        expect:
            converter.isAccepted(ramlFile)
    }

    def "should reject raml files"() {
        given:
            File invalidPact = new File(RamlContractConverterSpec.getResource("/invalid_api.raml").toURI())
        expect:
            converter.isAccepted(invalidPact) == false
    }

    def "should convert from raml to contract"() {
        given:
            Contract expectedContract = Contract.make {
                description("a retrieve Mallory request a user with username 'username' and password 'password' exists")
                request {
                    method(GET())
                    url("/mallory") {
                        queryParameters {
                            parameter("name", "ron")
                            parameter("status", "good")
                        }
                    }
                    headers {
                        contentType(applicationJson())
                    }
                    body(id: "123", method: "create")
                }
                response {
                    status(200)
                    headers {
                        contentType(applicationJson())
                    }
                    body([[
                            [email: "rddtGwwWMEhnkAPEmsyE",
                            id: "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
                            userName: "AJQrokEGPAVdOHprQpKP"]
                          ]])
                }
            }
        when:
            Collection<Contract> contracts = converter.convertFrom(ramlFile)
        then:
            contracts == [expectedContract]
    }

}