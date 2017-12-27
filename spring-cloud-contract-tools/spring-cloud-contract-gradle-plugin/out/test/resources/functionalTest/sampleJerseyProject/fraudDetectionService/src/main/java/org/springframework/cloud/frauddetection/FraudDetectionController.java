package org.springframework.cloud.frauddetection;

import org.springframework.cloud.frauddetection.model.FraudCheck;
import org.springframework.cloud.frauddetection.model.FraudCheckResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
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
	public FraudCheckResult fraudCheck(@RequestBody(required = false) FraudCheck fraudCheck) {
		if (amountGreaterThanThreshold(fraudCheck)) {
			return new FraudCheckResult(FRAUD, AMOUNT_TOO_HIGH);
		}
		return new FraudCheckResult(OK, NO_REASON);
	}

	private boolean amountGreaterThanThreshold(FraudCheck fraudCheck) {
		return MAX_AMOUNT.compareTo(fraudCheck.getLoanAmount()) < 0;
	}

}
