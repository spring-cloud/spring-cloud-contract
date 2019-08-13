/*
 * Copyright 2013-2019 the original author or authors.
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

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.*

fun URL.toByte(): ByteArray {
	return Files.readAllBytes(File(toURI()).toPath())
}

data class Test(val status: String)

@RestController
class TestController {

	private val request: ByteArray = javaClass.getResource("/contracts/binary/request.pdf").toByte()
	private val response: ByteArray = javaClass.getResource("/contracts/binary/response.pdf").toByte()

	@PostMapping("/tests")
	fun createNew(@RequestPart file1: MultipartFile,
				  @RequestPart file2: MultipartFile,
				  @RequestPart test: Test): Test {
		return Test("ok");
	}

	@PutMapping("/1", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
	fun response(@RequestBody requestBody: ByteArray): ByteArray {
		if (!Arrays.equals(this.request, requestBody)) {
			throw IllegalStateException("Invalid request body");
		}
		return response;
	}

}