package io.codearte.accurest.maven;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestProperties;
import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({ "3.3.3" })
public class PluginIntegrationTest {

	@Rule
	public final TestResources resources = new TestResources();

	private final TestProperties properties = new TestProperties();

	private final MavenRuntime maven;

	public PluginIntegrationTest(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
		this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
	}

	@Test
	public void shouldBuildSimpleBootWIthAccurtesProject() throws Exception {
		File basedir = resources.getBasedir("bootSimple");
		maven.forProject(basedir)
				.execute("package")
				.assertErrorFreeLog()
				.assertLogText("Accurest Plugin: Invoking test sources generation")
				.assertLogText("Generated 1 test classes.")
				.assertLogText("Accurest Plugin: Invoking GroovyDSL to WireMock client stubs conversion")
				.assertLogText("Creating new json")
				.assertErrorFreeLog();
	}

	@Test
	public void shouldConvertAccurestToWireMockStubs() throws Exception {
		File basedir = resources.getBasedir("pomless");
		properties.getPluginVersion();
		maven.forProject(basedir)
				.withCliOption("-X")
				.execute(String.format("io.codearte.accurest:accurest-maven-plugin:%s:convert", properties.getPluginVersion()))
				.assertLogText("Converting from accurest contracts written in GroovyDSL to WireMock stubs mappings")
				.assertLogText("Creating new json")
				.assertErrorFreeLog();
	}
}