package com.blogspot.toomuchcoding.frauddetection.model;

public class FraudServiceResponse {

	private FraudCheckStatus fraudCheckStatus;

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
