package io.codearte.accurest.samples.spring

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jms.annotation.EnableJms

@SpringBootApplication
@EnableJms
class SpringMessagingApplication {

	static void main(String[] args) {
		SpringApplication.run(SpringMessagingApplication.class, args)
	}
}
