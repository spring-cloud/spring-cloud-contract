package org.springframework.cloud.contract.wiremock;

import java.net.URI;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
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

	public String pom() {
		return this.restTemplate.exchange(
				RequestEntity.get(URI.create(this.base + "/pom.xml"))
						.accept(mediaTypes()).build(), String.class).getBody();
	}

	private MediaType[] mediaTypes() {
		return Stream
				.of("text/plain", "text/plain", "application/json", "application/json",
						"application/*+json", "application/*+json", "*/*", "*/*")
				.map(MediaType::valueOf).toArray(MediaType[]::new);
	}

	public void setBase(String base) {
		this.base = base;
	}
}
