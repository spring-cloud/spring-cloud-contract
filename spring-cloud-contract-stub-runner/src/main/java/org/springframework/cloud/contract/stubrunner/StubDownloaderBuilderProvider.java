package org.springframework.cloud.contract.stubrunner;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Provider for {@link StubDownloaderBuilder}. It can also pick a default
 * downloader if none is provided
 *
 * @author Marcin Grzejszczak
 * @since 1.1.0
 */
public class StubDownloaderBuilderProvider {

	private static final Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass());

	private final List<StubDownloaderBuilder> builders = new ArrayList<>();

	public StubDownloaderBuilderProvider() {
		this.builders.addAll(
				SpringFactoriesLoader.loadFactories(StubDownloaderBuilder.class, null));
	}

	public StubDownloaderBuilder get() {
		return this.builders.isEmpty() ? null : this.builders.get(0);
	}

	/**
	 * If a {@link StubDownloaderBuilder} is present will build a {@link StubDownloader} from it.
	 * If not will return the defaults basing on the {@link StubRunnerOptions} values
	 */
	public StubDownloader getOrDefaultDownloader(StubRunnerOptions stubRunnerOptions) {
		if (hasBuilder()) {
			log.info("A custom Stub Downloader was passed - will pick [" + get() + "]");
			return get().build(stubRunnerOptions);
		}
		if (stubRunnerOptions.stubsMode == StubRunnerProperties.StubsMode.CLASSPATH) {
			log.info("Classpath scanning will be used due to passed properties");
			return new ClasspathStubProvider().build(stubRunnerOptions);
		}
		log.info("Will download stubs using Aether");
		return new AetherStubDownloader(stubRunnerOptions);
	}

	public boolean hasBuilder() {
		return get() != null;
	}
}
