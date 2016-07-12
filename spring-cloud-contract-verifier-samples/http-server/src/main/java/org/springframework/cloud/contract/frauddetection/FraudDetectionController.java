package org.springframework.cloud.contract.frauddetection;

import org.springframework.cloud.contract.frauddetection.model.FraudCheck;
import org.springframework.cloud.contract.frauddetection.model.FraudCheckResult;
import org.springframework.cloud.contract.frauddetection.model.FraudCheckStatus;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

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
			return new FraudCheckResult(FraudCheckStatus.FRAUD, AMOUNT_TOO_HIGH);
		}
		return new FraudCheckResult(FraudCheckStatus.OK, NO_REASON);
	}

	private boolean amountGreaterThanThreshold(FraudCheck fraudCheck) {
		return MAX_AMOUNT.compareTo(fraudCheck.getLoanAmount()) < 0;
	}

}
