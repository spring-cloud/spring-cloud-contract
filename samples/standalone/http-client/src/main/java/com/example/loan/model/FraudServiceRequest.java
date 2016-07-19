package com.example.loan.model;

import java.math.BigDecimal;

public class FraudServiceRequest {

	private String clientId;

	private BigDecimal loanAmount;

	public FraudServiceRequest() {
	}

	public FraudServiceRequest(LoanApplication loanApplication) {
		this.clientId = loanApplication.getClient().getPesel();
		this.loanAmount = loanApplication.getAmount();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public BigDecimal getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(BigDecimal loanAmount) {
		this.loanAmount = loanAmount;
	}
}
