package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.BDDAssertions;
import org.junit.Test;

/**
 * @author Marcin Grzejszczak
 */
public class CompositeStubDownloaderBuilderTests {

	@Test public void should_delegate_work_to_other_stub_downloaders() {
		EmptyStubDownloaderBuilder emptyStubDownloaderBuilder = new EmptyStubDownloaderBuilder();
		ImpossibleToBuildStubDownloaderBuilder impossible = new ImpossibleToBuildStubDownloaderBuilder();
		List<StubDownloaderBuilder> builders = Arrays.asList(emptyStubDownloaderBuilder, impossible, new SomeStubDownloaderBuilder());
		CompositeStubDownloaderBuilder builder = new CompositeStubDownloaderBuilder(builders);
		StubDownloader downloader = builder.build(new StubRunnerOptionsBuilder().build());

		Map.Entry<StubConfiguration, File> entry = downloader
				.downloadAndUnpackStubJar(new StubConfiguration("a:b:v"));

		BDDAssertions.then(entry).isNotNull();
		BDDAssertions.then(emptyStubDownloaderBuilder.downloaderCalled()).isTrue();
		BDDAssertions.then(impossible.called).isTrue();
	}

	@Test public void should_return_null_if_no_builders_were_passed() {
		CompositeStubDownloaderBuilder builder = new CompositeStubDownloaderBuilder(null);

		StubDownloader downloader = builder.build(new StubRunnerOptionsBuilder().build());

		BDDAssertions.then(downloader).isNull();
	}
}

class EmptyStubDownloaderBuilder implements StubDownloaderBuilder {

	EmptyStubDownloader emptyStubDownloader;

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		this.emptyStubDownloader = new EmptyStubDownloader();
		return this.emptyStubDownloader;
	}

	boolean downloaderCalled() {
		return this.emptyStubDownloader.called;
	}
}

class ImpossibleToBuildStubDownloaderBuilder implements StubDownloaderBuilder {

	boolean called;

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		this.called = true;
		return null;
	}

}

class EmptyStubDownloader implements StubDownloader {
	boolean called;

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		this.called = true;
		return null;
	}
}

class SomeStubDownloaderBuilder implements StubDownloaderBuilder {

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		return new SomeStubDownloader();
	}
}

class SomeStubDownloader implements StubDownloader {
	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		return new AbstractMap.SimpleEntry<>(stubConfiguration, new File("."));
	}
}