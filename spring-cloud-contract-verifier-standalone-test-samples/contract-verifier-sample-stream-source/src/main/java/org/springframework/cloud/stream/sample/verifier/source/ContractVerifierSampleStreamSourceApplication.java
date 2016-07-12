package org.springframework.cloud.stream.sample.verifier.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
@EnableBinding(Source.class)
public class ContractVerifierSampleStreamSourceApplication {

	@Autowired
	Source source;

	public static void main(String[] args) {
		SpringApplication.run(ContractVerifierSampleStreamSourceApplication.class, args);
	}

	public void poll() {
		source.output().send(MessageBuilder.withPayload("{\"id\":\"99\",\"temperature\":\"123.45\"}").build());
	}
}
