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

package org.springframework.cloud.contract.stubrunner;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import org.springframework.core.io.Resource;

/**
 * @author Matty A
 */
public class FileStubDownloaderTests {

	@Test
	public void resolve() {
		final FileStubDownloader fileStubDownloader = new FileStubDownloader();
		Resource expectedUnixResource = new StubsResource("stubs://file:///User/A/B/C");
		Resource expectedWindowsResource = new StubsResource(
				"stubs://file:///C:/Users/A/B/C");
		String unixFileFormat = "stubs://file:///User/A/B/C";
		String windowsFileFormat = "stubs://file://C:\\Users\\A\\B\\C";
		String windowsFileFormatCorrectPathStart = "stubs://file:///C:\\Users\\A\\B\\C";
		Assertions.assertThat(expectedUnixResource)
				.isEqualTo(fileStubDownloader.resolve(unixFileFormat, null));
		Assertions.assertThat(expectedWindowsResource)
				.isEqualTo(fileStubDownloader.resolve(windowsFileFormat, null));
		Assertions.assertThat(expectedWindowsResource).isEqualTo(
				fileStubDownloader.resolve(windowsFileFormatCorrectPathStart, null));
	}

}
