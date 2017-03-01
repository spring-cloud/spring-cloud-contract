package com.example.fraud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraudStatsController {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 = "application/vnd.fraud.v1+json";

	private final StatsProvider statsProvider;

	public FraudStatsController(StatsProvider statsProvider) {
		this.statsProvider = statsProvider;
	}

	@GetMapping(
			value = "/frauds",
			produces = FRAUD_SERVICE_JSON_VERSION_1)
	public Response countAllFrauds() {
		return new Response(this.statsProvider.count(FraudType.ALL));
	}

	@GetMapping(
			value = "/drunks",
			produces = FRAUD_SERVICE_JSON_VERSION_1)
	public Response countAllDrunks() {
		return new Response(this.statsProvider.count(FraudType.DRUNKS));
	}
}

enum FraudType {
	DRUNKS, ALL
}

interface StatsProvider {
	int count(FraudType fraudType);
}

class Response {
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
