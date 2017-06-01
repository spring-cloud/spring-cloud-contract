package org.springframework.cloud.contract.verifier.spec.raml

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.raml.emitter.RamlEmitter
import org.raml.model.Raml
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.springframework.boot.test.rule.OutputCapture
import org.springframework.cloud.contract.spec.Contract
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Eddú Meléndez
 * @author Marcin Grzejszczak
 */
class RamlContractConverterSpec extends Specification {

	File ramlFile = new File(RamlContractConverterSpec.getResource("/api.raml").toURI())
	@Subject RamlContractConverter converter = new RamlContractConverter()
	@Rule TemporaryFolder tmp = new TemporaryFolder()
	@Rule OutputCapture outputCapture = new OutputCapture()

	def "should accept raml files"() {
		expect:
			converter.isAccepted(ramlFile)
	}

	def "should reject invalid raml files"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/invalid_api.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("took place while trying to parse RAML")
	}

	def "should reject a valid raml that has version 0.8"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/0_8_api.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("Spring Cloud Contract supports only RAML in version 1.0")
	}

	def "should reject a valid raml that doesn't have an example in request body"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/api_without_example_in_request_body.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("exampleIsPresentInRequestBody = [false]")
	}

	def "should reject a valid raml that doesn't have a default value in request headers"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/api_without_default_value_in_request_headers.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("defaultValuesPresentInRequestHeaders = [false]")
	}

	def "should reject a valid raml that doesn't have a default value in query parameters"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/api_without_default_value_in_query_params.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("defaultValuesPresentInRequestQueryParams = [false]")
	}

	def "should reject a valid raml that doesn't have an example in response body"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/api_without_example_in_response_body.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("exampleIsPresentInResponseBody = [false]")
	}

	def "should reject a valid raml that doesn't have a default value in response headers"() {
		given:
			File invalidPact = new File(RamlContractConverterSpec.getResource("/api_without_default_value_in_response_headers.raml").toURI())
		expect:
			converter.isAccepted(invalidPact) == false
		and:
			outputCapture.toString().contains("defaultValuesPresentInResponseHeaders = [false]")
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
		and:
			File newFile = tmp.newFile()
		when:
			Raml raml = converter.convertTo([contract])
		then:
			String ramlAsFile = new RamlEmitter().dump(raml)
		and:
			ramlAsFile == expectedYaml
		when:
			newFile.text = ramlAsFile
		then:
			RamlModelResult result = new RamlModelBuilder().buildApi(newFile)
			!result.hasErrors()
	}

}