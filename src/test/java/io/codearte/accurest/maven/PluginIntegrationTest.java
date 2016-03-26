package io.codearte.accurest.maven;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({ "3.3.3" })
public class PluginIntegrationTest {

	@Rule
	public final TestResources resources = new TestResources();

	private final MavenRuntime maven;

	public PluginIntegrationTest(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
		this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
	}

	@Test
	public void test() throws Exception {
		File basedir = resources.getBasedir("basic-with-execution");
		maven.forProject(basedir)
				.withCliOption("-X")
				.execute("package")
				.assertErrorFreeLog()
				.assertLogText("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion");
	}
}