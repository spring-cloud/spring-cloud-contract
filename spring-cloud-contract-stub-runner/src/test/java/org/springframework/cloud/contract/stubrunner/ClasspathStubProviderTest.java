package org.springframework.cloud.contract.stubrunner;

import org.junit.Test;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class ClasspathStubProviderTest {

	@Test public void should_return_null_if_stub_mode_is_not_classpath() {
		StubDownloader stubDownloader = new ClasspathStubProvider().build(new StubRunnerOptionsBuilder().withStubsMode(
				StubRunnerProperties.StubsMode.REMOTE).build());

		then(stubDownloader).isNull();
	}
}