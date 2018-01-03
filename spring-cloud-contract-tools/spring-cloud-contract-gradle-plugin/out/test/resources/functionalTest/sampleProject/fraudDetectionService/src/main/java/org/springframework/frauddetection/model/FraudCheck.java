package org.springframework.cloud.frauddetection.model;

import java.math.BigDecimal;

public class FraudCheck {

	private String clientPesel;

	private BigDecimal loanAmount;

	public FraudCheck() {
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
