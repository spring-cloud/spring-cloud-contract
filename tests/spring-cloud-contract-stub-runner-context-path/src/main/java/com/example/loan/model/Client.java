package com.example.loan.model;

public class Client {

	private String clientId;
	
	public Client() {
	}

	public Client(String clientId) {
		this.clientId = clientId;
	}

	public String getClientId() {
		return this.clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
