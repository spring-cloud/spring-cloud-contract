package com.example.loan.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FraudServiceResponse {

	private FraudCheckStatus fraudCheckStatus;

	@JsonProperty("rejection.reason")
	private String rejectionReason;

	public FraudServiceResponse() {
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
