package org.springframework.cloud.contract.stubrunner;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
class CompositeStubDownloaderBuilder implements StubDownloaderBuilder {

	private final List<StubDownloaderBuilder> builders;

	CompositeStubDownloaderBuilder(List<StubDownloaderBuilder> builders) {
		this.builders = builders;
	}

	@Override public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		if (this.builders == null) {
			return null;
		}
		return new CompositeStubDownloader(this.builders, stubRunnerOptions);
	}
}

class CompositeStubDownloader implements StubDownloader {

	private static final Log log = LogFactory.getLog(CompositeStubDownloader.class);

	private final List<StubDownloaderBuilder> builders;
	private final StubRunnerOptions stubRunnerOptions;

	CompositeStubDownloader(List<StubDownloaderBuilder> builders,
			StubRunnerOptions stubRunnerOptions) {
		this.builders = builders;
		this.stubRunnerOptions = stubRunnerOptions;
	}

	@Override public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
			StubConfiguration stubConfiguration) {
		for (StubDownloaderBuilder builder : this.builders) {
			StubDownloader downloader = builder.build(this.stubRunnerOptions);
			if (downloader == null) {
				continue;
			}
			Map.Entry<StubConfiguration, File> entry = downloader
					.downloadAndUnpackStubJar(stubConfiguration);
			if (entry != null) {
				return entry;
			}
		}
		return null;
	}
}
