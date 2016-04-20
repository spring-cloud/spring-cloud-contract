package io.codearte.accurest.samples.book

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ImportResource

@SpringBootApplication
@ImportResource("classpath*:integration-context.xml")
class IntegrationMessagingApplication {

	static void main(String[] args) {
		SpringApplication.run(IntegrationMessagingApplication.class, args)
	}
}
