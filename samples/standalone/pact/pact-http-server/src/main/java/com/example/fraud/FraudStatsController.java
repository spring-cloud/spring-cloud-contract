/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fraud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

enum FraudType {

	DRUNKS, ALL

}

interface StatsProvider {

	int count(FraudType fraudType);

}

@RestController
public class FraudStatsController {

	private static final String FRAUD_SERVICE_JSON_VERSION_1 = "application/vnd.fraud.v1+json";

	private final StatsProvider statsProvider;

	public FraudStatsController(StatsProvider statsProvider) {
		this.statsProvider = statsProvider;
	}

	@GetMapping(value = "/frauds", produces = FRAUD_SERVICE_JSON_VERSION_1)
	public Response countAllFrauds() {
		return new Response(this.statsProvider.count(FraudType.ALL));
	}

	@GetMapping(value = "/drunks", produces = FRAUD_SERVICE_JSON_VERSION_1)
	public Response countAllDrunks() {
		return new Response(this.statsProvider.count(FraudType.DRUNKS));
	}

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
