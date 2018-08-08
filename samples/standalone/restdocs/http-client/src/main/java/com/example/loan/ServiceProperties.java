package com.example.loan;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service")
public class ServiceProperties {

	private int port = 8080;

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}
}
