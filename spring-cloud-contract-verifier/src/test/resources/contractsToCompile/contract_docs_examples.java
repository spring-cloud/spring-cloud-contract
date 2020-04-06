
/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

// tag::class[]
class contract_docs_examples implements Supplier<Collection<Contract>> {

	org.springframework.cloud.contract.spec.Contract httpDsl =
			// tag::http_dsl[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				// Definition of HTTP request part of the contract
				// (this can be a valid request or invalid depending
				// on type of contract being specified).
				c.request(r -> {
					r.method(r.GET());
					r.url("/foo");
					// ...
				});

				// Definition of HTTP response part of the contract
				// (a service implementing this contract should respond
				// with following response after receiving request
				// specified in "request" part above).
				c.response(r -> {
					r.status(200);
					// ...
				});

				// Contract priority, which can be used for overriding
				// contracts (1 is highest). Priority is optional.
				c.priority(1);
			});

	// end::http_dsl[]

	org.springframework.cloud.contract.spec.Contract methodDsl = org.springframework.cloud.contract.spec.Contract
			.make(c -> {
				c.request(r -> {
					// tag::method[]
					r.method(r.GET());
					// end::method[]
					r.url("/foo");
				});

				c.response(r -> {
					r.status(200);
				});

				c.priority(1);
			});

	org.springframework.cloud.contract.spec.Contract request =
			// tag::request[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// HTTP request method (GET/POST/PUT/DELETE).
					r.method("GET");

					// Path component of request URL is specified as follows.
					r.urlPath("/users");
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::request[]

	org.springframework.cloud.contract.spec.Contract url =
			// tag::url[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					r.method("GET");

					// Specifying `url` and `urlPath` in one contract is illegal.
					r.url("http://localhost:8888/users");
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::url[]

	org.springframework.cloud.contract.spec.Contract urlPaths =
			// tag::urlpath[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());

					r.urlPath("/users", u -> {

						// Each parameter is specified in form
						// `'paramName' : paramValue` where parameter value
						// may be a simple literal or one of matcher functions,
						// all of which are used in this example.
						u.queryParameters(q -> {

							// If a simple literal is used as value
							// default matcher function is used (equalTo)
							q.parameter("limit", 100);

							// `equalTo` function simply compares passed value
							// using identity operator (==).
							q.parameter("filter", r.equalTo("email"));

							// `containing` function matches strings
							// that contains passed substring.
							q.parameter("gender",
									r.value(r.consumer(r.containing("[mf]")),
											r.producer("mf")));

							// `matching` function tests parameter
							// against passed regular expression.
							q.parameter("offset",
									r.value(r.consumer(r.matching("[0-9]+")),
											r.producer(123)));

							// `notMatching` functions tests if parameter
							// does not match passed regular expression.
							q.parameter("loginStartsWith",
									r.value(r.consumer(r.notMatching(".{0,2}")),
											r.producer(3)));
						});
					});

					// ...
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::urlpath[]

	org.springframework.cloud.contract.spec.Contract headers =
			// tag::headers[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());
					r.url("/foo");

					// Each header is added in form `'Header-Name' : 'Header-Value'`.
					// there are also some helper methods
					r.headers(h -> {
						h.header("key", "value");
						h.contentType(h.applicationJson());
					});

					// ...
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::headers[]

	org.springframework.cloud.contract.spec.Contract cookies =
			// tag::cookies[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());
					r.url("/foo");

					// Each Cookies is added in form `'Cookie-Key' : 'Cookie-Value'`.
					// there are also some helper methods
					r.cookies(ck -> {
						ck.cookie("key", "value");
						ck.cookie("another_key", "another_value");
					});

					// ...
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::cookies[]

	org.springframework.cloud.contract.spec.Contract body =
			// tag::body[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());
					r.url("/foo");

					// Currently only JSON format of request body is supported.
					// Format will be determined from a header or body's content.
					r.body("{ \"login\" : \"john\", \"name\": \"John The Contract\" }");
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::body[]

	org.springframework.cloud.contract.spec.Contract bodyAsXml =
			// tag::bodyAsXml[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());
					r.url("/foo");

					// In this case body will be formatted as XML.
					r.body(r.equalToXml(
							"<user><login>john</login><name>John The Contract</name></user>"));
				});

				c.response(r -> {
					// ...
					r.status(200);
				});
			});

	// end::bodyAsXml[]

	org.springframework.cloud.contract.spec.Contract response =
			// tag::response[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					// ...
					r.method(r.GET());
					r.url("/foo");
				});
				c.response(r -> {
					// Status code sent by the server
					// in response to request specified above.
					r.status(r.OK());
				});
			});

	// end::response[]

	org.springframework.cloud.contract.spec.Contract regex =
			// tag::regex[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					r.method("GET");
					r.url(r.$(r.consumer(r.regex("\\/[0-9]{2}")), r.producer("/12")));
				});
				c.response(r -> {
					r.status(r.OK());
					r.body(ContractVerifierUtil.map().entry("id", r.$(r.anyNumber()))
							.entry("surname", r.$(r.consumer("Kowalsky"),
									r.producer(r.regex("[a-zA-Z]+")))));
					r.headers(h -> {
						h.header("Content-Type", "text/plain");
					});
				});
			});

	// end::regex[]

	org.springframework.cloud.contract.spec.Contract optionals =
			// tag::optionals[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.priority(1);
				c.name("optionals");
				c.request(r -> {
					r.method("POST");
					r.url("/users/password");
					r.headers(h -> {
						h.contentType(h.applicationJson());
					});
					r.body(ContractVerifierUtil.map()
							.entry("email",
									r.$(r.consumer(r.optional(r.regex(r.email()))),
											r.producer("abc@abc.com")))
							.entry("callback_url", r.$(r.consumer(r.regex(r.hostname())),
									r.producer("https://partners.com"))));
				});
				c.response(r -> {
					r.status(404);
					r.headers(h -> {
						h.header("Content-Type", "application/json");
					});
					r.body(ContractVerifierUtil.map().entry("code", r.value(
							r.consumer("123123"), r.producer(r.optional("123123")))));
				});
			});

	// end::optionals[]

	org.springframework.cloud.contract.spec.Contract method =
			// tag::methodBuilder[]
			org.springframework.cloud.contract.spec.Contract.make(c -> {
				c.request(r -> {
					r.method("PUT");
					r.url(r.$(r.consumer(r.regex("^/api/[0-9]{2}$")),
							r.producer("/api/12")));
					r.headers(h -> {
						h.header("Content-Type", "application/json");
					});
					r.body("[{\"text\": \"Gonna see you at Warsaw\" }]");
				});
				c.response(r -> {
					r.body(ContractVerifierUtil.map()
							.entry("path",
									r.$(r.consumer("/api/12"),
											r.producer(r.regex("^/api/[0-9]{2}$"))))
							.entry("correlationId", r.$(r.consumer("1223456"), r
									.producer(r.execute("isProperCorrelationId($it)")))));
					r.status(r.OK());
				});
			});

	// end::methodBuilder[]

	@Override
	public Collection<Contract> get() {
		return Collections.singletonList(method);
	}

}
// end::class[]