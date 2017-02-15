package org.springframework.cloud.contract.verifier.spec.raml

import org.raml.emitter.RamlEmitter
import org.raml.model.Raml
import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Eddú Meléndez
 */
class RamlContractConverterSpec extends Specification {

	File ramlFile = new File(RamlContractConverterSpec.getResource("/api.raml").toURI())
	@Subject RamlContractConverter converter = new RamlContractConverter()

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
								  [email   : "rddtGwwWMEhnkAPEmsyE",
								   id      : "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
								   userName: "AJQrokEGPAVdOHprQpKP"]
						  ]])
				}
			}
		when:
			Collection<Contract> contracts = converter.convertFrom(ramlFile)
		then:
			contracts == [expectedContract]
	}

	def "should convert from contract to raml"() {
		given:
			Contract contract = Contract.make {
				description("a retrieve Mallory request a user with username 'username' and password 'password' exists")
				request {
					method GET()
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
								  [email   : "rddtGwwWMEhnkAPEmsyE",
								   id      : "eb0f8c17-c06a-479e-9204-14f7c95b63a6",
								   userName: "AJQrokEGPAVdOHprQpKP"]
						  ]])
				}
			}
		and:
			String expectedYaml = """#%RAML 0.8
title: "A generated RAML from Spring Cloud Contract"
"/mallory": 
    get: 
        description: "a retrieve Mallory request a user with username 'username' and password 'password' exists"
        headers: 
            "Content-Type": 
                type: string
                required: false
                repeat: false
                default: "application/json"
        queryParameters: 
            "name": 
                type: string
                required: false
                repeat: false
                default: "ron"
            "status": 
                type: string
                required: false
                repeat: false
                default: "good"
        body: 
            "application/json": 
                example: '{"id":"123","method":"create"}'
        responses: 
            "200": 
                body: 
                    "application/json": 
                        example: '[[{"email":"rddtGwwWMEhnkAPEmsyE","id":"eb0f8c17-c06a-479e-9204-14f7c95b63a6","userName":"AJQrokEGPAVdOHprQpKP"}]]'
                headers: 
                    "Content-Type": 
                        type: string
                        required: false
                        repeat: false
                        default: "application/json"
"""
		when:
			Raml raml = converter.convertTo([contract])
		then:
			String ramlAsFile = new RamlEmitter().dump(raml)
		and:
			ramlAsFile == expectedYaml
	}

}