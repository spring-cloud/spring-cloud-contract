package com.example.loan.model;

public class Client {

	private String pesel;
	
	public Client() {
	}

	public Client(String pesel) {
		this.pesel = pesel;
	}

	public String getPesel() {
		return pesel;
	}

	public void setPesel(String pesel) {
		this.pesel = pesel;
	}
}
