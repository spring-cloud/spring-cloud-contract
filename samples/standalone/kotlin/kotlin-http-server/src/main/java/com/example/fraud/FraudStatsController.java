package com.example.fraud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraudStatsController {

	private final StatsProvider statsProvider;

	public FraudStatsController(StatsProvider statsProvider) {
		this.statsProvider = statsProvider;
	}

	@GetMapping(value = "/frauds")
	public Response countAllFrauds() {
		return new Response(this.statsProvider.count(FraudType.ALL));
	}

	@GetMapping(value = "/drunks")
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
