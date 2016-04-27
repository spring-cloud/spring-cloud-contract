package io.codearte.accurest.stubrunner.boot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author Marcin Grzejszczak
 */
@SpringBootApplication
class StubRunnerBoot {

	static void main(String[] args) {
		SpringApplication.run(StubRunnerBoot.class, args);
	}
}
