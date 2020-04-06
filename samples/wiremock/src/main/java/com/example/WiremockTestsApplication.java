/*
 * Copyright 2013-2020 the original author or authors.
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

package com.example;

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

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import({ Service.class, Controller.class })
public class WiremockTestsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WiremockTestsApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}

@RestController
class Controller {

	private final Service service;

	Controller(Service service) {
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
	String base;

	private RestTemplate restTemplate;

	Service(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String go() {
		return this.restTemplate.getForEntity(this.base + "/test", String.class)
				.getBody();
	}

	public String pom() {
		return this.restTemplate
				.exchange(RequestEntity.get(URI.create(this.base + "/pom.xml"))
						.accept(mediaTypes()).build(), String.class)
				.getBody();
	}

	private MediaType[] mediaTypes() {
		return Stream
				.of("text/plain", "text/plain", "application/json", "application/json",
						"application/*+json", "application/*+json", "*/*", "*/*")
				.map(MediaType::valueOf).toArray(MediaType[]::new);
	}

	public String go2() {
		return this.restTemplate.getForEntity(this.base + "/test2", String.class)
				.getBody();
	}

	public void setBase(String base) {
		this.base = base;
	}

}
