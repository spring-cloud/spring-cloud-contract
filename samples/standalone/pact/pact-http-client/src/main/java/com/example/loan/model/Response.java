package com.example.loan.model;

public class Response {
	private int count;

	public Response(int count) {
		this.count = count;
	}

	public Response() {
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}