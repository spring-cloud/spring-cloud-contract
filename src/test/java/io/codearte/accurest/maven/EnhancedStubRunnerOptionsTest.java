package io.codearte.accurest.maven;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import io.codearte.accurest.stubrunner.StubRunnerOptions;

public class EnhancedStubRunnerOptionsTest {

	private EnhancedStubRunnerOptionsBuilder builder = new EnhancedStubRunnerOptionsBuilder(new StubRunnerOptions());

	@Test
	public void shouldAddStubCoordinatesWithPort() throws Exception {

		//given
		builder.withStubs("foo:bar:8080");

		//when
		EnhancedStubRunnerOptions options = builder.build();

		//then
		assertThat(options.getDependencies())
				.hasToString("[foo:bar:+:stubs]");
		assertThat(options.getStubIdsToPortMapping())
				.hasToString("{foo:bar:+:stubs=8080}");
	}

	@Test
	public void shouldAddStubCoordinatesWithoutPort() throws Exception {

		//given
		builder.withStubs("foo:bar");

		//when
		EnhancedStubRunnerOptions options = builder.build();

		//then
		assertThat(options.getDependencies())
				.hasToString("[foo:bar:+:stubs]");
		assertThat(options.getStubIdsToPortMapping())
				.hasSize(0);
	}

	@Test
	public void shouldAddMultipleStubCoordinates() throws Exception {

		//given
		builder.withStubs("foo:bar,bar:foo");

		//when
		EnhancedStubRunnerOptions options = builder.build();

		//then
		assertThat(options.getDependencies())
				.hasSize(2);
	}

	@Test
	public void shouldAddStubCoordinatesWithVersion() throws Exception {

		//given
		builder.withStubs("foo:1.2.3:bar");

		//when
		EnhancedStubRunnerOptions options = builder.build();

		//then
		assertThat(options.getDependencies())
				.hasToString("[foo:1.2.3:bar:stubs]");
	}
}