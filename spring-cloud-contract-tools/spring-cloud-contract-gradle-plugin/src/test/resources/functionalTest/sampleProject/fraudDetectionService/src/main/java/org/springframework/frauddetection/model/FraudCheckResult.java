package org.springframework.cloud.frauddetection.model;

public class FraudCheckResult {

	private FraudCheckStatus fraudCheckStatus;

	private String rejectionReason;

	public FraudCheckResult() {
	}

	public FraudCheckResult(FraudCheckStatus fraudCheckStatus, String rejectionReason) {
		this.fraudCheckStatus = fraudCheckStatus;
		this.rejectionReason = rejectionReason;
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
