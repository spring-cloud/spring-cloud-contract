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

package org.springframework.cloud.contract.verifier.util.xml;

import java.util.LinkedList;

class FieldAssertion extends XmlAsserter {

	FieldAssertion(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer,
			LinkedList<String> specialCaseXPathBuffer, Object value,
			XmlAsserterConfiguration xmlAsserterConfiguration) {
		super(cachedObjects, xPathBuffer, specialCaseXPathBuffer, value, xmlAsserterConfiguration);
	}

	FieldAssertion(XmlAsserter asserter) {
		super(asserter);
	}

}
