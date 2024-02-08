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

package com.example.fraud;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
class MessageSender {

	private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

	private final StreamBridge streamBridge;

	private final byte[] expectedOutput;

	MessageSender(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
		this.expectedOutput = forFile("/contracts/messaging/output.pdf");
	}

	public void emit() {
		log.info("Emitting the message");
		this.streamBridge.send("sensor_data-out-0", "{\"id\":\"99\",\"temperature\":\"123.45\"}");
	}

	public void emitBytes() {
		log.info("Emitting the message");
		this.streamBridge.send("my_output-out-0", this.expectedOutput);
	}

	private byte[] forFile(String relative) {
		URL resource = MessageSender.class.getResource(relative);
		try {
			return Files.readAllBytes(new File(resource.toURI()).toPath());
		}
		catch (IOException | URISyntaxException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
