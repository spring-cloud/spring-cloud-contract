package io.codearte.accurest.maven;

import static io.takari.maven.testing.TestMavenRuntime.newParameter;
import static io.takari.maven.testing.TestResources.assertFileContents;
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
		maven.executeMojo(basedir, "convert");
		assertFilesPresent(basedir, "target/accurest/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateWireMockFromStubsDirectory() throws Exception {
		File basedir = resources.getBasedir("withStubs");
		maven.executeMojo(basedir, "convert", newParameter("contractsDirectory", "src/test/resources/stubs"));
		assertFilesPresent(basedir, "target/accurest/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateWireMockStubsInSelectedLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "convert", newParameter("outputDirectory", "target/foo"));
		assertFilesPresent(basedir, "target/foo/mappings/Sample.json");
	}

	@Test
	public void shouldGenerateContractSpecificationInDefaultLocation() throws Exception {
		File basedir = resources.getBasedir("basic");
		maven.executeMojo(basedir, "generateTests", newParameter("testFramework", "SPOCK"));
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

	@Test
	public void shouldGenerateStubs() throws Exception {
		File basedir = resources.getBasedir("generatedStubs");
		maven.executeMojo(basedir, "generateStubs");
		assertFilesPresent(basedir, "target/sample-project-0.1-stubs.jar");
	}

	@Test
	public void shouldGenerateStubsWithMappingsOnly() throws Exception {
		File basedir = resources.getBasedir("generatedStubs");
		maven.executeMojo(basedir, "generateStubs", newParameter("attachContracts", "false"));
		assertFilesPresent(basedir, "target/sample-project-0.1-stubs.jar");
		// FIXME: add assertion for jar content
	}

	@Test
	public void shouldGenerateStubsWithCustomClassifier() throws Exception {
		File basedir = resources.getBasedir("generatedStubs");
		maven.executeMojo(basedir, "generateStubs", newParameter("classifier", "foo"));
		assertFilesPresent(basedir, "target/sample-project-0.1-foo.jar");
	}


}
