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

package org.springframework.cloud.contract.spec.internal


import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
interface RegexCreatingProperty<T extends DslProperty> {

	//tag::regex_creating_props[]
	T anyAlphaUnicode()

	T anyAlphaNumeric()

	T anyNumber()

	T anyInteger()

	T anyPositiveInt()

	T anyDouble()

	T anyHex()

	T aBoolean()

	T anyIpAddress()

	T anyHostname()

	T anyEmail()

	T anyUrl()

	T anyHttpsUrl()

	T anyUuid()

	T anyDate()

	T anyDateTime()

	T anyTime()

	T anyIso8601WithOffset()

	T anyNonBlankString()

	T anyNonEmptyString()

	T anyOf(String... values)
	//end::regex_creating_props[]
}
