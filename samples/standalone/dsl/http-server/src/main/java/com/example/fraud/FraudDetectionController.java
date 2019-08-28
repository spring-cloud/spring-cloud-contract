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

import java.math.BigDecimal;

import com.example.fraud.model.FraudCheck;
import com.example.fraud.model.FraudCheckResult;
import com.example.fraud.model.FraudCheckStatus;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class FraudDetectionController {

	private static final String NO_REASON = null;

	private static final String AMOUNT_TOO_HIGH = "Amount too high";

	private static final BigDecimal MAX_AMOUNT = new BigDecimal("5000");

	// tag::server_api[]
	@RequestMapping(value = "/fraudcheck", method = PUT)
	public FraudCheckResult fraudCheck(@RequestBody FraudCheck fraudCheck) {
		// end::server_api[]
		// tag::new_impl[]
		if (amountGreaterThanThreshold(fraudCheck)) {
			return new FraudCheckResult(FraudCheckStatus.FRAUD, AMOUNT_TOO_HIGH);
		}
		// end::new_impl[]
		// tag::initial_impl[]
		return new FraudCheckResult(FraudCheckStatus.OK, NO_REASON);
		// end::initial_impl[]
	}

	@RequestMapping(value = "/pactfraudcheck", method = PUT)
	public FraudCheckResult pactFraudCheck(@RequestBody FraudCheck fraudCheck) {
		return fraudCheck(fraudCheck);
	}

	@RequestMapping(value = "/yamlfraudcheck", method = PUT)
	public FraudCheckResult yamlFraudCheck(@RequestBody FraudCheck fraudCheck) {
		return fraudCheck(fraudCheck);
	}

	private boolean amountGreaterThanThreshold(FraudCheck fraudCheck) {
		return MAX_AMOUNT.compareTo(fraudCheck.getLoanAmount()) < 0;
	}

}
