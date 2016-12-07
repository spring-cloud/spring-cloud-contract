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
	public int countAllFrauds() {
		return this.statsProvider.count(FraudType.ALL);
	}

	@GetMapping(
			value = "/drunks",
			produces = FRAUD_SERVICE_JSON_VERSION_1)
	public int countAllDrunks() {
		return this.statsProvider.count(FraudType.DRUNKS);
	}

}

enum FraudType {
	DRUNKS, ALL
}

interface StatsProvider {
	int count(FraudType fraudType);
}
