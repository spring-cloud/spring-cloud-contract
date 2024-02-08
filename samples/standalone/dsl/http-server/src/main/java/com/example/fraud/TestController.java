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
import java.util.Arrays;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {

	private final byte[] request;

	private final byte[] response;

	public TestController() {
		this.request = toByte(
				TestController.class.getResource("/contracts/binary/request.pdf"));
		this.response = toByte(
				TestController.class.getResource("/contracts/binary/response.pdf"));
	}

	private byte[] toByte(URL url) {
		try {
			return Files.readAllBytes(new File(url.toURI()).toPath());
		}
		catch (URISyntaxException | IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@PostMapping("/tests")
	public Test createNew(@RequestPart MultipartFile file1,
			@RequestPart MultipartFile file2, @RequestPart Test test) {
		return new Test("ok");
	}

	@GetMapping("/example")
	public void example() {
	}

	@PutMapping(value = "/1", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public byte[] response(@RequestBody byte[] requestBody) {
		if (!Arrays.equals(this.request, requestBody)) {
			throw new IllegalStateException("Invalid request body");
		}
		return this.response;
	}

}

class Test {

	private String status;

	public Test(String status) {
		this.status = status;
	}

	public Test() {
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
