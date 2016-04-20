package io.codearte.accurest.samples.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;

@SpringBootApplication
@EnableBinding([Source, Sink])
class StreamMessagingApplication {

	static void main(String[] args) {
		SpringApplication.run(StreamMessagingApplication.class, args)
	}
}
