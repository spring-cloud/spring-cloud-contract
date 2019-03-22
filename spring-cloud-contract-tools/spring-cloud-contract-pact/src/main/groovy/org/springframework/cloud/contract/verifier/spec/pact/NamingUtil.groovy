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

package org.springframework.cloud.contract.verifier.spec.pact

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import org.springframework.cloud.contract.spec.Contract

/**
 * @author Marcin Grzejszczak
 * @since
 */
@PackageScope
@CompileStatic
final class NamingUtil {

	// consumer___producer___testname
	private static final String SEPARATOR = "___"

	protected static Names name(Contract contract) {
		String contractName = contract.name
		if (!contractName || !contractName.contains(SEPARATOR)) {
			return new Names(["Consumer", "Provider", ""] as String[])
		}
		return new Names(contractName.split(SEPARATOR))
	}
}

@PackageScope
@CompileStatic
class Names {
	final String consumer
	final String producer
	final String test

	Names(String[] strings) {
		this.consumer = strings[0]
		this.producer = strings[1]
		this.test = strings.length >= 2 ? strings[2] : ""
	}


	@Override
	String toString() {
		return this.consumer + "_" + this.producer
	}
}
