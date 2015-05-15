package com.blogspot.toomuchcoding.frauddetection.model;

public class LoanApplicationResult {

	private LoanApplicationStatus loanApplicationStatus;

	private String rejectionReason;

	public LoanApplicationResult() {
	}

	public LoanApplicationResult(LoanApplicationStatus loanApplicationStatus, String rejectionReason) {
		this.loanApplicationStatus = loanApplicationStatus;
		this.rejectionReason = rejectionReason;
	}

	public LoanApplicationStatus getLoanApplicationStatus() {
		return loanApplicationStatus;
	}

	public void setLoanApplicationStatus(LoanApplicationStatus loanApplicationStatus) {
		this.loanApplicationStatus = loanApplicationStatus;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
