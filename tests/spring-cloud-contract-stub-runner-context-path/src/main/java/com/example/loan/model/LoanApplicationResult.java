package com.example.loan.model;

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
		return this.loanApplicationStatus;
	}

	public void setLoanApplicationStatus(LoanApplicationStatus loanApplicationStatus) {
		this.loanApplicationStatus = loanApplicationStatus;
	}

	public String getRejectionReason() {
		return this.rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}
}
