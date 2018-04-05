package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.Map;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

/**
 * @author Marcin Grzejszczak
 */
public class S3StubDownloaderTests {

	static {
		System.setProperty("aws.region", "eu-central-1");
		System.setProperty("aws.region.static", "eu-central-1");
		System.setProperty("aws.region.auto", "false");
	}

	@Test
	public void shouldWork() {
		StubDownloaderBuilder stubDownloaderBuilder = new S3StubDownloaderBuilder();
		StubDownloader stubDownloader = stubDownloaderBuilder.build(new StubRunnerOptionsBuilder()
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
				.withStubRepositoryRoot("s3://spring-cloud-contract/")
				.build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(
						new StubConfiguration("com.example", "bookstore",
								"0.0.1.RELEASE"));

		BDDAssertions.then(entry).isNotNull();
	}

}