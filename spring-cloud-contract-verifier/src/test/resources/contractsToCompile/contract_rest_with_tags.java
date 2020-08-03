
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

// tag::class[]

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

// tag::class[]
class contract_rest_with_tags implements Supplier<Collection<Contract>> {

	static Object description = Collections.singletonList(
			// tag::description[]
			Contract.make(c -> {
				c.description("Some description");
			}));

	// end::description[]

	static Object name = Collections.singletonList(
			// tag::name[]
			Contract.make(c -> {
				c.name("some name");
			}));

	// end::name[]

	static Object ignored = Collections.singletonList(
			// tag::ignored[]
			Contract.make(c -> {
				c.ignored();
			}));

	// end::ignored[]

	static Object in_progress = Collections.singletonList(
			// tag::in_progress[]
			Contract.make(c -> {
				c.inProgress();
			}));

	// end::in_progress[]

	static Object metadata = Collections.singletonList(
			// tag::metadata[]
			Contract.make(c -> {
				Map<String, Object> map = new HashMap<>();
				map.put("wiremock", "{ \"response\" : { \"fixedDelayMilliseconds\" : 2000 } }");
				c.metadata(map);
			}));

	// end::metadata[]

	@Override
	public Collection<Contract> get() {
		return Collections.singletonList(Contract.make(c -> {
			c.description("Some description");
			c.name("some name");
			c.priority(8);
			c.ignored();
			c.inProgress();
			c.request(r -> {
				r.url("/foo", u -> {
					u.queryParameters(q -> {
						q.parameter("a", "b");
						q.parameter("b", "c");
					});
				});
				r.method(r.PUT());
				r.headers(h -> {
					h.header("foo", r.value(r.client(r.regex("bar")), r.server("bar")));
					h.header("fooReq", "baz");
				});
				r.body(ContractVerifierUtil.map().entry("foo", "bar"));
				r.bodyMatchers(m -> {
					m.jsonPath("$.foo", m.byRegex("bar"));
				});
			});
			c.response(r -> {
				r.fixedDelayMilliseconds(1000);
				r.status(r.OK());
				r.headers(h -> {
					h.header("foo2", r.value(r.server(r.regex("bar")), r.client("bar")));
					h.header("foo3", r.value(r.server(r.execute("andMeToo($it)")),
							r.client("foo33")));
					h.header("fooRes", "baz");
				});
				r.body(ContractVerifierUtil.map().entry("foo2", "bar")
						.entry("foo3", "baz").entry("nullValue", null));
				r.bodyMatchers(m -> {
					m.jsonPath("$.foo2", m.byRegex("bar"));
					m.jsonPath("$.foo3", m.byCommand("executeMe($it)"));
					m.jsonPath("$.nullValue", m.byNull());
				});
			});
		}));
	}

}
// end::class[]
