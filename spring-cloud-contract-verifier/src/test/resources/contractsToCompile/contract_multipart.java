
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
import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.util.ContractVerifierUtil;

// tag::class[]
class contract_multipart implements Supplier<Collection<Contract>> {

	private static Map<String, DslProperty> namedProps(Request r) {
		Map<String, DslProperty> map = new HashMap<>();
		// name of the file
		map.put("name", r.$(r.c(r.regex(r.nonEmpty())), r.p("filename.csv")));
		// content of the file
		map.put("content", r.$(r.c(r.regex(r.nonEmpty())), r.p("file content")));
		// content type for the part
		map.put("contentType", r.$(r.c(r.regex(r.nonEmpty())), r.p("application/json")));
		return map;
	}

	@Override
	public Collection<Contract> get() {
		return Collections.singletonList(Contract.make(c -> {
			c.request(r -> {
				r.method("PUT");
				r.url("/multipart");
				r.headers(h -> {
					h.contentType("multipart/form-data;boundary=AaB03x");
				});
				r.multipart(ContractVerifierUtil.map()
						// key (parameter name), value (parameter value) pair
						.entry("formParameter",
								r.$(r.c(r.regex("\".+\"")),
										r.p("\"formParameterValue\"")))
						.entry("someBooleanParameter",
								r.$(r.c(r.regex(r.anyBoolean())), r.p("true")))
						// a named parameter (e.g. with `file` name) that represents file
						// with
						// `name` and `content`. You can also call `named("fileName",
						// "fileContent")`
						.entry("file", r.named(namedProps(r))));
			});
			c.response(r -> {
				r.status(r.OK());
			});
		}));
	}

}
// end::class[]
