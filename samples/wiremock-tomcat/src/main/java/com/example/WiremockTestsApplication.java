package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WiremockTestsApplication {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(WiremockTestsApplication.class, args);
	}
}

@RestController
class Controller {

	private final Service service;

	public Controller(Service service) {
		this.service = service;
	}

	@RequestMapping("/")
	public String home() {
		return this.service.go();
	}
}

@Component
class Service {

	@Value("${app.baseUrl:https://example.org}")
	private String base;

	private final RestTemplate restTemplate;

	public Service(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String go() {
		return this.restTemplate.getForEntity(this.base + "/resource", String.class).getBody();
	}
}
