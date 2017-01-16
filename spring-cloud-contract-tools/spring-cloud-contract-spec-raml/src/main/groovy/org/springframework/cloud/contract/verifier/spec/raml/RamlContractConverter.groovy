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
import groovy.transform.PackageScope
import org.raml.model.Action
import org.raml.model.ActionType
import org.raml.model.Raml2
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.raml.v2.api.model.v10.bodies.Response
import org.raml.v2.api.model.v10.datamodel.ExampleSpec
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration
import org.raml.v2.api.model.v10.methods.Method
import org.raml.v2.api.model.v10.resources.Resource
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.ContractConverter
import org.springframework.cloud.contract.spec.internal.QueryParameter
import org.springframework.cloud.contract.spec.internal.QueryParameters
import org.springframework.cloud.contract.spec.internal.RegexPatterns

import java.util.regex.Pattern
/**
 * Converter of RAML file
 *
 * @author Eddú Meléndez
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
@CompileStatic
class RamlContractConverter implements ContractConverter<Raml2> {

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

    @Override
    Raml2 convertTo(Collection<Contract> contracts) {
        Raml2 raml2 = new Raml2()
        Contract.make {
            request {
                urlPath("asd") {

                }
            }
        }
        raml2.setTitle("A generated RAML from Spring Cloud Contract")
        raml2.resources = contracts.findAll { it.request }.collectEntries { Contract contract ->
            org.raml.model.Resource resource = new org.raml.model.Resource()
            resource.relativeUri = url(contract)
            ActionType actionType = ActionType.valueOf(contract.request.method.serverValue as String)
            Action action = new Action()
            resource.actions << [(actionType) : action]
            QueryParameters queryParams = queryParams(contract)
            if (queryParams) {
                action.queryParameters = queryParams.parameters.collectEntries { QueryParameter param ->
                    return [(param.name) : new org.raml.model.parameter.QueryParameter(type: RamlType.fromObject(param.serverValue).ramlName)]
                }
            }
            action.description = contract.description
            //TODO: how to deal with body?

        }
        return raml2
    }

    @PackageScope
    enum RamlType {
        ANY("any"), NUMBER("number"), BOOLEAN("boolean"), STRING("string"),
        DATE_ONLY("date-only"), TIME_ONLY("time-only"), DATETIME_ONLY("datetime-only"),
        DATETIME("datetime"), INTEGER("integer"), NIL("nil")

        final String ramlName
        private static Pattern ISO_DATE = Pattern.compile(new RegexPatterns().isoDate())
        private static Pattern ISO_DATE_TIME = Pattern.compile(new RegexPatterns().isoDateTime())
        private static Pattern ISO_TIME = Pattern.compile(new RegexPatterns().isoTime())

        RamlType(String name) {
            this.ramlName = name
        }

        static RamlType fromObject(Object o) {
            if (o == null) {
                return NIL
            }
            switch (o.class) {
                case Boolean:
                    return BOOLEAN
                case String:
                    String string = o as String
                    if (ISO_DATE.matcher(string).matches()) {
                        return DATE_ONLY
                    } else if (ISO_DATE_TIME.matcher(string).matches()) {
                        return DATETIME
                    } else if (ISO_TIME.matcher(string).matches()) {
                        return TIME_ONLY
                    }
                    return STRING
                case Integer:
                    return INTEGER
                case Number:
                    return NUMBER
                default:
                    return ANY
            }
        }


        @Override
        String toString() {
            return this.ramlName
        }
    }

    protected String url(Contract dsl) {
        if (dsl.request.urlPath) {
            return dsl.request.urlPath.serverValue.toString()
        } else if (dsl.request.url) {
            return dsl.request.url.serverValue.toString()
        }
        throw new IllegalStateException("No url provided")
    }

    protected QueryParameters queryParams(Contract dsl) {
        if (dsl.request.urlPath) {
            return dsl.request.urlPath.queryParameters
        } else if (dsl.request.url) {
            return dsl.request.url.queryParameters
        }
        throw new IllegalStateException("No url provided")
    }
}