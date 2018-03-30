package org.springframework.cloud.contract.stubrunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Provider for {@link StubDownloaderBuilder}. It can also pick a default
 * downloader if none is provided
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class StubDownloaderBuilderProvider {

	private final List<StubDownloaderBuilder> builders = new ArrayList<>();

	public StubDownloaderBuilderProvider() {
		this.builders.addAll(
				SpringFactoriesLoader.loadFactories(StubDownloaderBuilder.class, null));
	}

	StubDownloaderBuilderProvider(List<StubDownloaderBuilder> builders) {
		this.builders.addAll(builders);
	}

	/**
	 * @param stubRunnerOptions
	 * @param additionalBuilders - optional array of {@link StubDownloaderBuilder}s to append to the list of builders
	 * @return composite {@link StubDownloader} that iterates over a list of stub downloaders
	 */
	public StubDownloader get(StubRunnerOptions stubRunnerOptions,
			StubDownloaderBuilder... additionalBuilders) {
		List<StubDownloaderBuilder> builders = this.builders;
		if (additionalBuilders != null) {
			builders.addAll(Arrays.asList(additionalBuilders));
		}
		List<StubDownloaderBuilder> defaultBuilders = defaultStubDownloaderBuilders();
		builders.addAll(defaultBuilders);
		return new CompositeStubDownloader(builders, stubRunnerOptions);
	}

	List<StubDownloaderBuilder> defaultStubDownloaderBuilders() {
		return Arrays
					.asList(new ScmStubDownloaderBuilder(), new ClasspathStubProvider(),
							new AetherStubDownloaderBuilder());
	}
}
