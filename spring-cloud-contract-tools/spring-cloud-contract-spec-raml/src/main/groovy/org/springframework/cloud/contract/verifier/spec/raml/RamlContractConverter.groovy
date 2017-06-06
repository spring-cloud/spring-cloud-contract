/*
 *  Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.contract.verifier.spec.raml

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.raml.v2.api.model.v10.bodies.Response
import org.raml.v2.api.model.v10.datamodel.ExampleSpec
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration
import org.raml.v2.api.model.v10.methods.Method
import org.raml.v2.api.model.v10.resources.Resource
import org.raml.v2.internal.impl.RamlBuilder
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.Headers

/**
 * Converter of RAML file
 *
 * @author Eddú Meléndez
 * @since 1.1.0
 */
@CompileStatic
class RamlContractConverter implements ContractConverter<RamlBuilder> {

    @Override
    boolean isAccepted(File file) {
        RamlModelBuilder ramlContract = new RamlModelBuilder()
        def api = ramlContract.buildApi(file)
        return !api.hasErrors()
    }

    @Override
    Collection<Contract> convertFrom(File file) {
        RamlModelBuilder ramlBuilder = new RamlModelBuilder()
        RamlModelResult api = ramlBuilder.buildApi(file)
        return api.apiV10.resources().collect { Resource resource ->
            Contract.make {
                description(resource.description().value())
                request {
                    url(resource.relativeUri().value()) {
                        queryParameters {
                            resource.methods().each { Method httpMethod ->
                                httpMethod.queryParameters().each { TypeDeclaration queryParam ->
                                    parameter(queryParam.name(), queryParam.defaultValue())
                                }
                            }
                        }
                    }

                    resource.methods().each { Method httpMethod ->
                        method(httpMethod.method().toUpperCase())

                        httpMethod.body().each { TypeDeclaration requestBody ->
                            JsonSlurper parser = new JsonSlurper()
                            if (requestBody.defaultValue()) {
                                def parsedBody = parser.parseText(requestBody.defaultValue())
                                if (parsedBody instanceof Map) {
                                    body(parsedBody as Map)
                                } else if (parsedBody instanceof List) {
                                    body(parsedBody as List)
                                } else {
                                    body(parsedBody.toString())
                                }
                            } else if (requestBody.example()) {
                                def parsedBody = parser.parseText(requestBody.example().value())
                                if (parsedBody instanceof Map) {
                                    body(parsedBody as Map)
                                } else if (parsedBody instanceof List) {
                                    body(parsedBody as List)
                                } else {
                                    body(parsedBody.toString())
                                }
                            } else if (requestBody.examples()) {
                                requestBody.examples().each { ExampleSpec example ->
                                    def parsedBody = parser.parseText(example.value())
                                    if (parsedBody instanceof Map) {
                                        body(parsedBody as Map)
                                    } else if (parsedBody instanceof List) {
                                        body(parsedBody as List)
                                    } else {
                                        body(parsedBody.toString())
                                    }
                                }
                            }
                        }
                    }

                    resource.methods().each { Method method ->
                        method.headers().each { TypeDeclaration httpHeader ->
                            headers {
                                header(httpHeader.name(), httpHeader.defaultValue())
                            }
                        }
                    }
                }
                response {
                    status(Integer.valueOf(resource.methods().first().responses().first().code().value()))

                    resource.methods().each { Method method ->
                        method.responses().each { Response response ->
                            response.body().each { TypeDeclaration responseBody ->
                                JsonSlurper parser = new JsonSlurper()
                                if (responseBody.defaultValue()) {
                                    def parsedBody = parser.parseText(responseBody.defaultValue())
                                    if (parsedBody instanceof Map) {
                                        body(parsedBody as Map)
                                    } else if (parsedBody instanceof List) {
                                        body(parsedBody as List)
                                    } else {
                                        body(parsedBody.toString())
                                    }
                                } else if (responseBody.example()) {
                                    def parsedBody = parser.parseText(responseBody.example().value())
                                    if (parsedBody instanceof Map) {
                                        body(parsedBody as Map)
                                    } else if (parsedBody instanceof List) {
                                        body(parsedBody as List)
                                    } else {
                                        body(parsedBody.toString())
                                    }
                                } else if (responseBody.examples()) {
                                    responseBody.examples().each { ExampleSpec example ->
                                        def parsedBody = parser.parseText(example.value())
                                        if (parsedBody instanceof Map) {
                                            body(parsedBody as Map)
                                        } else if (parsedBody instanceof List) {
                                            body(parsedBody as List)
                                        } else {
                                            body(parsedBody.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    resource.methods().each { Method method ->
                        method.responses().each { Response response ->
                            response.headers().each { TypeDeclaration httpHeader ->
                                headers {
                                    header(httpHeader.name(), httpHeader.defaultValue())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected Map<String, String> headers(Headers headers, Closure closure) {
        return headers.entries.collectEntries {
            String name = it.name
            String value = closure(it)
            return [(name) : value]
        }
    }

    @Override
    RamlBuilder convertTo(Collection<Contract> contracts) {
        return null
    }
}