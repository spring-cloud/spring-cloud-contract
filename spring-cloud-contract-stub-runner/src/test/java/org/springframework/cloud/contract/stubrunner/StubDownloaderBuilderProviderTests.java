package org.springframework.cloud.contract.stubrunner;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Marcin Grzejszczak
 */
@RunWith(MockitoJUnitRunner.class)
public class StubDownloaderBuilderProviderTests {

	@Mock StubDownloaderBuilder one;
	@Mock StubDownloaderBuilder two;
	@Mock StubDownloaderBuilder three;

	@Test public void should_get_providers_from_factories_default_and_additional_ones() {
		StubDownloaderBuilderProvider provider = new StubDownloaderBuilderProvider(Collections.singletonList(one)) {
			@Override List<StubDownloaderBuilder> defaultStubDownloaderBuilders() {
				return Collections.singletonList(two);
			}
		};
		StubRunnerOptions options = new StubRunnerOptionsBuilder().build();

		provider.get(options, three)
				.downloadAndUnpackStubJar(new StubConfiguration("a:b:c"));

		BDDMockito.then(one).should().build(options);
		BDDMockito.then(two).should().build(options);
		BDDMockito.then(three).should().build(options);
	}
}