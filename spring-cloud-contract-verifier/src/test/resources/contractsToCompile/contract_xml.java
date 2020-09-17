
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
import java.util.function.Supplier;

import org.springframework.cloud.contract.spec.Contract;

class contract_xml implements Supplier<Contract> {

	@Override
	public Contract get() {
		return Contract.make(c -> {
			c.request(r -> {
				r.method(r.GET());
				r.urlPath("/get");
				r.headers(h -> {
					h.contentType(h.applicationXml());
				});
			});
			c.response(r -> {
				r.status(r.OK());
				r.headers(h -> {
					h.contentType(h.applicationXml());
				});
				r.body("<test>\n" + "<duck type='xtype'>123</duck>\n" + "<alpha>abc</alpha>\n" + "<list>\n"
						+ "<elem>abc</elem>\n" + "<elem>def</elem>\n" + "<elem>ghi</elem>\n" + "</list>\n"
						+ "<number>123</number>\n" + "<aBoolean>true</aBoolean>\n" + "<date>2017-01-01</date>\n"
						+ "<dateTime>2017-01-01T01:23:45</dateTime>\n" + "<time>01:02:34</time>\n"
						+ "<valueWithoutAMatcher>foo</valueWithoutAMatcher>\n" + "<key><complex>foo</complex></key>\n"
						+ "</test>");
				r.bodyMatchers(m -> {
					m.xPath("/test/duck/text()", m.byRegex("[0-9]{3}"));
					m.xPath("/test/duck/text()", m.byCommand("equals($it)"));
					m.xPath("/test/duck/xxx", m.byNull());
					m.xPath("/test/duck/text()", m.byEquality());
					m.xPath("/test/alpha/text()", m.byRegex(r.onlyAlphaUnicode()));
					m.xPath("/test/alpha/text()", m.byEquality());
					m.xPath("/test/number/text()", m.byRegex(r.number()));
					m.xPath("/test/date/text()", m.byDate());
					m.xPath("/test/dateTime/text()", m.byTimestamp());
					m.xPath("/test/time/text()", m.byTime());
					m.xPath("/test/*/complex/text()", m.byEquality());
					m.xPath("/test/duck/@type", m.byEquality());
				});
			});
		});
	};

}
// end::class[]
