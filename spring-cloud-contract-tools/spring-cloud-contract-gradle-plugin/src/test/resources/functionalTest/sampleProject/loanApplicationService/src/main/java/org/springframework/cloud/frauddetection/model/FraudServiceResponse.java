package org.springframework.cloud.frauddetection.model;

public class FraudServiceResponse {

	private FraudCheckStatus fraudCheckStatus;

	private String rejectionReason;

	public FraudServiceResponse() {
	}

	public FraudCheckStatus getFraudCheckStatus() {
		return this.fraudCheckStatus;
	}

	public void setFraudCheckStatus(FraudCheckStatus fraudCheckStatus) {
		this.fraudCheckStatus = fraudCheckStatus;
	}

	public String getRejectionReason() {
		return this.rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
