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

package org.springframework.cloud.contract.verifier.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

interface RestAssuredVerifier {

	Log log = LogFactory.getLog(RestAssuredVerifier.class);

	// TODO: Remove in next major
	String REST_ASSURED_2_0_CLASS = "com.jayway.restassured.RestAssured";

	ClassPresenceChecker checker = new ClassPresenceChecker();

	@Deprecated
	default boolean isRestAssured2Present() {
		boolean restAssured2Present = checker.isClassPresent(REST_ASSURED_2_0_CLASS);
		if (restAssured2Present) {
			log.warn(
					"Rest Assured 2 found on the classpath. Please upgrade to the latest version of Rest Assured");
		}
		return restAssured2Present;

	}

}

class ClassPresenceChecker {

	private static final Log log = LogFactory.getLog(ClassPresenceChecker.class);

	boolean isClassPresent(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException ex) {
			if (log.isTraceEnabled()) {
				log.trace("[" + className + "] is not present on classpath");
			}
			return false;
		}
	}

}
