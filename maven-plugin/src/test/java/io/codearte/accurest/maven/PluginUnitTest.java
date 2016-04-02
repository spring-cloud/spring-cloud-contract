package io.codearte.accurest.maven;

import static io.takari.maven.testing.TestMavenRuntime.newParameter;
import static io.takari.maven.testing.TestResources.assertFilesPresent;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;

public class PluginUnitTest {

	@Rule
	public final TestResources resources = new TestResources();

	@Rule
	public final TestMavenRuntime maven = new TestMavenRuntime();

	@Test
	public void shouldGenerateWireMockStubsInDefaultLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "generateStubs");
		assertFilesPresent(basedir, "target/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateWireMockFromStubsDirectory() throws Exception {
		File basedir = resources.getBasedir("withStubs");
		maven.executeMojo(basedir, "generateStubs", newParameter("contractsDir", "/src/test/resources/stubs"));
		assertFilesPresent(basedir, "target/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateWireMockStubsInSelectedLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "generateStubs", newParameter("mappingsDir", "foo"));
		assertFilesPresent(basedir, "target/foo/Sample.json");
	}

	@Test
	public void shouldGenerateContractSpecificationInDefaultLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "generateSpecs");
		assertFilesPresent(basedir,
				"target/generated-test-sources/accurest/io/codearte/accurest/tests/AccurestSpec.groovy");
	}

	@Test
	public void shouldGenerateContractTestsInDefaultLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "generateTests");
		assertFilesPresent(basedir,
				"target/generated-test-sources/accurest/io/codearte/accurest/tests/AccurestTest.java");
	}

}
