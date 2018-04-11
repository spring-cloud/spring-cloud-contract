package com.example.fraud;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marcin Grzejszczak
 */
@RestController
class FraudNameController {

	private final FraudVerifier fraudVerifier;

	FraudNameController(FraudVerifier fraudVerifier) {
		this.fraudVerifier = fraudVerifier;
	}

	@PutMapping(value = "/frauds/name")
	public NameResponse checkByName(@RequestBody NameRequest request) {
		boolean fraud = this.fraudVerifier.isFraudByName(request.getName());
		if (fraud) {
			return new NameResponse("Sorry " + request.getName() + " but you're a fraud");
		}
		return new NameResponse("Don't worry " + request.getName() + " you're not a fraud");
	}

	@GetMapping(value = "/frauds/name")
	public String checkByName(@CookieValue("name") String value,
			@CookieValue("name2") String value2) {
		return value + " " + value2;
	}
}

interface FraudVerifier {
	boolean isFraudByName(String name);
}

class NameRequest {
	private String name;

	public NameRequest(String name) {
		this.name = name;
	}

	public NameRequest() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

class NameResponse {
	private String result;

	public NameResponse(String result) {
		this.result = result;
	}

	public NameResponse() {
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
