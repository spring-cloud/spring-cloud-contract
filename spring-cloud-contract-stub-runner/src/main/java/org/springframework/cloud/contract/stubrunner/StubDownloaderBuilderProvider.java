package org.springframework.cloud.contract.stubrunner;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Provider for {@link StubDownloaderBuilder}
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

	public StubDownloaderBuilder get() {
		return this.builders.isEmpty() ? null : this.builders.get(0);
	}

	public boolean hasBuilder() {
		return get() != null;
	}
}
