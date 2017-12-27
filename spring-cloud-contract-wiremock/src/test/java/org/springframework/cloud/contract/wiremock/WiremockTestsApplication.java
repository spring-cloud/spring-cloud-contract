package org.springframework.cloud.contract.wiremock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration
@Import({Service.class, Controller.class})
public class WiremockTestsApplication {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
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

	@Value("${app.baseUrl:http://example.org}")
	private String base;

	private RestTemplate restTemplate;

	public Service(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String go() {
		return this.restTemplate.getForEntity(this.base + "/test", String.class).getBody();
	}

	public void setBase(String base) {
		this.base = base;
	}
}
