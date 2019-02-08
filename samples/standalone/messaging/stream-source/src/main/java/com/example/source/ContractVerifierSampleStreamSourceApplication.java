/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.source;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

interface MyProcessor extends Sink {

	String MY_OUTPUT = "my_output";

	@Output("my_output")
	MessageChannel output();

}

@SpringBootApplication
@EnableBinding({ Source.class, MyProcessor.class })
public class ContractVerifierSampleStreamSourceApplication {

	@Autowired
	Source source;

	public static void main(String[] args) {
		SpringApplication.run(ContractVerifierSampleStreamSourceApplication.class, args);
	}

	public void poll() {
		this.source.output().send(MessageBuilder
				.withPayload("{\"id\":\"99\",\"temperature\":\"123.45\"}").build());
	}

}

@Component
class MyProcessorListener {

	private static final Logger log = LoggerFactory.getLogger(MyProcessorListener.class);

	private final MyProcessor processor;

	private final byte[] expectedInput;

	private final byte[] expectedOutput;

	MyProcessorListener(MyProcessor processor) {
		this.processor = processor;
		this.expectedInput = forFile("/contracts/input.pdf");
		this.expectedOutput = forFile("/contracts/output.pdf");
	}

	private byte[] forFile(String relative) {
		URL resource = MyProcessorListener.class.getResource(relative);
		try {
			return Files.readAllBytes(new File(resource.toURI()).toPath());
		}
		catch (IOException | URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@StreamListener(Sink.INPUT)
	void listen(byte[] payload) {
		log.info("Got the message!");
		if (!Arrays.equals(payload, this.expectedInput)) {
			throw new IllegalStateException("Wrong input");
		}
		this.processor.output()
				.send(MessageBuilder.withPayload(this.expectedOutput).build());
	}

}
