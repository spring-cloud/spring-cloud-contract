/**
 *
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.blogspot.toomuchcoding.frauddetection;

import java.math.BigDecimal;

import com.blogspot.toomuchcoding.frauddetection.model.FraudCheck;
import com.blogspot.toomuchcoding.frauddetection.model.FraudCheckResult;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.blogspot.toomuchcoding.frauddetection.model.FraudCheckStatus.FRAUD;
import static com.blogspot.toomuchcoding.frauddetection.model.FraudCheckStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
public class FraudDetectionController {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 = "application/vnd.fraud.v1+json";
	private static final String NO_REASON = null;
	private static final String AMOUNT_TOO_HIGH = "Amount too high";
	private static final BigDecimal MAX_AMOUNT = new BigDecimal("5000");

	@RequestMapping(
			value = "/fraudcheck",
			method = PUT,
			consumes = FRAUD_SERVICE_JSON_VERSION_1,
			produces = FRAUD_SERVICE_JSON_VERSION_1)
	public FraudCheckResult fraudCheck(@RequestBody FraudCheck fraudCheck) {
		if (amountGreaterThanThreshold(fraudCheck)) {
			return new FraudCheckResult(FRAUD, AMOUNT_TOO_HIGH);
		}
		return new FraudCheckResult(OK, NO_REASON);
	}

	private boolean amountGreaterThanThreshold(FraudCheck fraudCheck) {
		return MAX_AMOUNT.compareTo(fraudCheck.getLoanAmount()) < 0;
	}

}
