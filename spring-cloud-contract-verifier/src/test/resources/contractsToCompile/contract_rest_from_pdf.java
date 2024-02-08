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
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

class contract_rest_from_pdf implements Supplier<Collection<Contract>> {

	@Override
	public Collection<Contract> get() {
		return Collections.singletonList(Contract.make(c -> {
			c.request(r -> {
				r.url("/1");
				r.method(r.PUT());
				r.body(r.fileAsBytes("request.pdf"));
				r.headers(h -> {
					h.contentType(h.applicationOctetStream());
				});
			});
			c.response(r -> {
				r.status(r.OK());
				r.body(r.fileAsBytes("response.pdf"));
				r.headers(h -> {
					h.contentType(h.applicationOctetStream());
				});
			});
		}));
	}

}
// end::class[]
