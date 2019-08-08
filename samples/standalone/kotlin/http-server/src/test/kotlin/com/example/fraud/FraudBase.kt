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

package com.example.fraud;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;

open class FraudBase {

	@Before
	fun setup() {
		RestAssuredMockMvc.standaloneSetup(FraudDetectionController(),
				FraudStatsController(stubbedStatsProvider()))
	}

	private fun stubbedStatsProvider(): StatsProvider {
		return object : StatsProvider {
			override fun count(fraudType: FraudType): Int {
				return when (fraudType) {
					FraudType.DRUNKS -> 100
					FraudType.ALL -> 200
					else -> 0
				}
			}
		}
	}

	fun assertThatRejectionReasonIsNull(rejectionReason: Any?) {
		assert(rejectionReason == null)
	}

}
