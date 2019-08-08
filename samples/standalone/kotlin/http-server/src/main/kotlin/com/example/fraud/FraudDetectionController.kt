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

import com.example.fraud.model.FraudCheck
import com.example.fraud.model.FraudCheckResult
import com.example.fraud.model.FraudCheckStatus
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class FraudDetectionController {

	private val maxAmount = BigDecimal("5000")

	// tag::server_api[]
	@PutMapping("/fraudcheck")
	fun fraudCheck(@RequestBody fraudCheck: FraudCheck): FraudCheckResult {
		// end::server_api[]
		// tag::new_impl[]
		if (amountGreaterThanThreshold(fraudCheck)) {
			return FraudCheckResult(FraudCheckStatus.FRAUD, "Amount too high")
		}
		// end::new_impl[]
		// tag::initial_impl[]
		return FraudCheckResult(FraudCheckStatus.OK)
		// end::initial_impl[]
	}

	fun amountGreaterThanThreshold(fraudCheck: FraudCheck): Boolean {
		return maxAmount < fraudCheck.loanAmount;
	}

}
