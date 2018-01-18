package com.example.fraud.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FraudCheckResult {

	private FraudCheckStatus fraudCheckStatus;

	@JsonProperty("rejection.reason")
	private String rejectionReason;

	public FraudCheckResult() {
	}

	public FraudCheckResult(FraudCheckStatus fraudCheckStatus, String rejectionReason) {
		this.fraudCheckStatus = fraudCheckStatus;
		this.rejectionReason = rejectionReason;
	}

	public FraudCheckStatus getFraudCheckStatus() {
		return fraudCheckStatus;
	}

	public void setFraudCheckStatus(FraudCheckStatus fraudCheckStatus) {
		this.fraudCheckStatus = fraudCheckStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
