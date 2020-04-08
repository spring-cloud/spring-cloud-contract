package org.springframework.cloud.contract.stubrunner;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.*;

class FileStubDownloaderTests {

	@Test
	void resolve() {
		final FileStubDownloader fileStubDownloader = new FileStubDownloader();
		Resource expectedUnixResource = new StubsResource("stubs://file:///User/A/B/C");
		Resource expectedWindowsResource = new StubsResource("stubs://file:///C:/Users/A/B/C");
		String unixFileFormat = "stubs://file:///User/A/B/C";
		String windowsFileFormat = "stubs://file://C:\\Users\\A\\B\\C";
		String windowsFileFormatCorrectPathStart = "stubs://file:///C:\\Users\\A\\B\\C";
		assertAll("Resolving of resource happens for all file formats",
				() -> assertEquals(expectedUnixResource, fileStubDownloader.resolve(unixFileFormat, null)),
				() -> assertEquals(expectedWindowsResource, fileStubDownloader.resolve(windowsFileFormat, null)),
				() -> assertEquals(expectedWindowsResource, fileStubDownloader.resolve(windowsFileFormatCorrectPathStart, null))
		);
	}
}