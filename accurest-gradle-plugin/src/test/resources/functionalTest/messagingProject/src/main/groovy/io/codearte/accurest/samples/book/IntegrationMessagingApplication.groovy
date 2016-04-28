package io.codearte.accurest.samples.book

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ImportResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
@ImportResource("classpath*:integration-context.xml")
class IntegrationMessagingApplication {

	@RequestMapping("/foo")
	String foo() {
		return "bar"
	}

	static void main(String[] args) {
		SpringApplication.run(IntegrationMessagingApplication.class, args)
	}
}
