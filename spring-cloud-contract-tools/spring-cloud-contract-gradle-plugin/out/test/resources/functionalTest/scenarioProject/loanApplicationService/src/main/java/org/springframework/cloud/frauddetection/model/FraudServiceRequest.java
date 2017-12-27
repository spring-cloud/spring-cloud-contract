package org.springframework.cloud.frauddetection.model;

import java.math.BigDecimal;

public class FraudServiceRequest {

	private String clientPesel;

	private BigDecimal loanAmount;

	public FraudServiceRequest() {
	}

	public FraudServiceRequest(LoanApplication loanApplication) {
		this.clientPesel = loanApplication.getClient().getPesel();
		this.loanAmount = loanApplication.getAmount();
	}

	public String getClientPesel() {
		return this.clientPesel;
	}

	public void setClientPesel(String clientPesel) {
		this.clientPesel = clientPesel;
	}

	public BigDecimal getLoanAmount() {
		return this.loanAmount;
	}

	public void setLoanAmount(BigDecimal loanAmount) {
		this.loanAmount = loanAmount;
	}
}
