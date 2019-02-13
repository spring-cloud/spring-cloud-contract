/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.frauddetection;

import java.math.BigDecimal;

import org.springframework.cloud.frauddetection.model.FraudCheck;
import org.springframework.cloud.frauddetection.model.FraudCheckResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;

import static org.springframework.cloud.frauddetection.model.FraudCheckStatus.FRAUD;
import static org.springframework.cloud.frauddetection.model.FraudCheckStatus.OK;

@Controller
@Path("/")
public class FraudDetectionController {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 = "application/vnd.fraud.v1+json";

	private static final String NO_REASON = null;

	private static final String AMOUNT_TOO_HIGH = "Amount too high";

	private static final BigDecimal MAX_AMOUNT = new BigDecimal("5000");

	@PUT
	@Path("/fraudcheck")
	@Produces(FRAUD_SERVICE_JSON_VERSION_1)
	@Consumes(FRAUD_SERVICE_JSON_VERSION_1)
	public FraudCheckResult fraudCheck(
			@RequestBody(required = false) FraudCheck fraudCheck) {
		if (amountGreaterThanThreshold(fraudCheck)) {
			return new FraudCheckResult(FRAUD, AMOUNT_TOO_HIGH);
		}
		return new FraudCheckResult(OK, NO_REASON);
	}

	@GET
	@Path("/frauds/name")
	public String cookie(@CookieParam("name") Cookie name,
			@CookieParam("name2") Cookie name2) {
		return name.getValue() + " " + name2.getValue();
	}

	private boolean amountGreaterThanThreshold(FraudCheck fraudCheck) {
		return MAX_AMOUNT.compareTo(fraudCheck.getLoanAmount()) < 0;
	}

}
