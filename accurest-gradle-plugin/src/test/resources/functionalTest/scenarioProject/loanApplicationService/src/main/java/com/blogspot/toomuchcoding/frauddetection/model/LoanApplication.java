package com.blogspot.toomuchcoding.frauddetection.model;

import java.math.BigDecimal;

public class LoanApplication {

	private Client client;

	private BigDecimal amount;

	private String loanApplicationId;

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getLoanApplicationId() {
		return loanApplicationId;
	}

	public void setLoanApplicationId(String loanApplicationId) {
		this.loanApplicationId = loanApplicationId;
	}
}
