/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package contracts

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract
import org.springframework.cloud.contract.spec.withQueryParameters

var httpDsl: org.springframework.cloud.contract.spec.Contract =
        // tag::http_dsl[]
        contract {
            // Definition of HTTP request part of the contract
            // (this can be a valid request or invalid depending
            // on type of contract being specified).
            request {
                method = GET
                url = url("/foo")
                // ...
            }

            // Definition of HTTP response part of the contract
            // (a service implementing this contract should respond
            // with following response after receiving request
            // specified in "request" part above).
            response {
                status = OK
                // ...
            }

            // Contract priority, which can be used for overriding
            // contracts (1 is highest). Priority is optional.
            priority = 1
        }
        // end::http_dsl[]


var request: org.springframework.cloud.contract.spec.Contract =
        // tag::request[]
        contract {
            request {
                // HTTP request method (GET/POST/PUT/DELETE).
                method = method("GET")

                // Path component of request URL is specified as follows.
                urlPath = path("/users")
            }
            response {
                // ...
                status = code(200)
            }
        }
        // end::request[]


var url: org.springframework.cloud.contract.spec.Contract =
        // tag::url[]
        contract {
            request {
                method = GET

                // Specifying `url` and `urlPath` in one contract is illegal.
                url("http://localhost:8888/users")
            }
            response {
                // ...
                status = OK
            }
        }
        // end::url[]

var urlPaths: org.springframework.cloud.contract.spec.Contract =
        // tag::urlpath[]
        contract {
            request {
                // ...
                method = GET

                // Each parameter is specified in form
                // `'paramName' : paramValue` where parameter value
                // may be a simple literal or one of matcher functions,
                // all of which are used in this example.
                urlPath = path("/users") withQueryParameters {
                    // If a simple literal is used as value
                    // default matcher function is used (equalTo)
                    parameter("limit", 100)

                    // `equalTo` function simply compares passed value
                    // using identity operator (==).
                    parameter("filter", equalTo("email"))

                    // `containing` function matches strings
                    // that contains passed substring.
                    parameter("gender", value(consumer(containing("[mf]")), producer("mf")))

                    // `matching` function tests parameter
                    // against passed regular expression.
                    parameter("offset", value(consumer(matching("[0-9]+")), producer(123)))

                    // `notMatching` functions tests if parameter
                    // does not match passed regular expression.
                    parameter("loginStartsWith", value(consumer(notMatching(".{0,2}")), producer(3)))
                }

                // ...
            }
            response {
                // ...
                status = code(200)
            }
        }
        // end::urlpath[]

var headers: org.springframework.cloud.contract.spec.Contract =
        // tag::headers[]
        contract {
            request {
                // ...
                method = GET
                url = url("/foo")

                // Each header is added in form `'Header-Name' : 'Header-Value'`.
                // there are also some helper variables
                headers {
                    header("key", "value")
                    contentType = APPLICATION_JSON
                }

                // ...
            }
            response {
                // ...
                status = OK
            }
        }
        // end::headers[]

var cookies: org.springframework.cloud.contract.spec.Contract =
        // tag::cookies[]
        contract {
            request {
                // ...
                method = GET
                url = url("/foo")

                // Each Cookies is added in form `'Cookie-Key' : 'Cookie-Value'`.
                // there are also some helper methods
                cookies {
                    cookie("key", "value")
                    cookie("another_key", "another_value")
                }

                // ...
            }

            response {
                // ...
                status = code(200)
            }
        }
        // end::cookies[]

var body: org.springframework.cloud.contract.spec.Contract =
        // tag::body[]
        contract {
            request {
                // ...
                method = GET
                url = url("/foo")

                // Currently only JSON format of request body is supported.
                // Format will be determined from a header or body's content.
                body = body("{ \"login\" : \"john\", \"name\": \"John The Contract\" }")
            }
            response {
                // ...
                status = OK
            }
        }
        // end::body[]

var response: org.springframework.cloud.contract.spec.Contract =
        // tag::response[]
        contract {
            request {
                // ...
                method = GET
                url =url("/foo")
            }
            response {
                // Status code sent by the server
                // in response to request specified above.
                status = OK
            }
        }
        // end::response[]

var regex: org.springframework.cloud.contract.spec.Contract =
        // tag::regex[]
        contract {
            request {
                method = method("GET")
                url = url(v(consumer(regex("\\/[0-9]{2}")), producer("/12")))
            }
            response {
                status = OK
                body(mapOf(
                        "id" to v(anyNumber),
                        "surname" to v(consumer("Kowalsky"), producer(regex("[a-zA-Z]+")))
                ))
                headers {
                    header("Content-Type", "text/plain")
                }
            }
        }
        // end::regex[]

var regexCreatingProps: org.springframework.cloud.contract.spec.Contract =
        // tag::regex_creating_props[]
        contract {
            name = "foo"
            label = "trigger_event"
            input {
                triggeredBy = "toString()"
            }
            outputMessage {
                sentTo = sentTo("topic.rateablequote")
                body(mapOf(
                        "alpha" to v(anyAlphaUnicode),
                        "number" to v(anyNumber),
                        "anInteger" to v(anyInteger),
                        "positiveInt" to v(anyPositiveInt),
                        "aDouble" to v(anyDouble),
                        "aBoolean" to v(aBoolean),
                        "ip" to v(anyIpAddress),
                        "hostname" to v(anyAlphaUnicode),
                        "email" to v(anyEmail),
                        "url" to v(anyUrl),
                        "httpsUrl" to v(anyHttpsUrl),
                        "uuid" to v(anyUuid),
                        "date" to v(anyDate),
                        "dateTime" to v(anyDateTime),
                        "time" to v(anyTime),
                        "iso8601WithOffset" to v(anyIso8601WithOffset),
                        "nonBlankString" to v(anyNonBlankString),
                        "nonEmptyString" to v(anyNonEmptyString),
                        "anyOf" to v(anyOf('foo', 'bar'))
                ))
                headers {
                    header("Content-Type", "text/plain")
                }
            }
        }
        //end::regex_creating_props[]

var optionals: org.springframework.cloud.contract.spec.Contract =
        // tag::optionals[]
        contract { c ->
            priority = 1
            name = "optionals"
            request {
                method = POST
                url = url("/users/password")
                headers {
                    contentType = APPLICATION_JSON
                }
                body = body(mapOf(
                        "email" to v(consumer(optional(regex(email))), producer("abc@abc.com")),
                        "callback_url" to v(consumer(regex(hostname)), producer("https://partners.com"))
                ))
            }
            response {
                status = NOT_FOUND
                headers {
                    header("Content-Type", "application/json")
                }
                body(mapOf(
                        "code" to value(consumer("123123"), producer(optional("123123")))
                ))
            }
        }
        // end::optionals[]

var methodDsl: org.springframework.cloud.contract.spec.Contract =
        contract { c ->
            request {
                // tag::method[]
                method = GET
                // end::method[]
                url = url("/foo")
            }
            response {
                status = NOT_FOUND
            }
            priority = 1
        }

var method: org.springframework.cloud.contract.spec.Contract =
        // tag::methodBuilder[]
        contract {
            request {
                method = PUT
                url = url(v(consumer(regex("^/api/[0-9]{2}$")), producer("/api/12")))
                headers {
                    header("Content-Type", "application/json")
                }
                body = body("[{\"text\": \"Gonna see you at Warsaw\" }]")
            }
            response {
                status = OK
                body = body(mapOf(
                        "path" to v(consumer("/api/12"), producer(regex("^/api/[0-9]{2}$"))),
                        "correlationId" to v(consumer("1223456"), producer(execute("isProperCorrelationId(\$it)")))
                ))
            }
        }
        // end::methodBuilder[]
