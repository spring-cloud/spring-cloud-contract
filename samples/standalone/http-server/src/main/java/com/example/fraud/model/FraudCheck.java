package com.example.fraud.model;

import java.math.BigDecimal;

public class FraudCheck {

	private String clientId;

	private BigDecimal loanAmount;

	public FraudCheck() {
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
