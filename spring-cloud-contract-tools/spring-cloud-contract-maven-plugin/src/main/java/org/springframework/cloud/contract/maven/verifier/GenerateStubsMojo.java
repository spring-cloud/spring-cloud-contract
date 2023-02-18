/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.maven.verifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import static org.springframework.cloud.contract.maven.verifier.ChangeDetector.inputFilesChangeDetected;

/**
 * Picks the converted .json files and creates a jar. Requires convert to be executed
 * first.
 *
 * @author Mariusz Smykula
 */
@Mojo(name = "generateStubs", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class GenerateStubsMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
	private File projectBuildDirectory;

	@Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
	private String projectFinalName;

	@Parameter(property = "stubsDirectory", defaultValue = "${project.build.directory}/stubs")
	private File outputDirectory;

	/**
	 * Set this to "true" to bypass the whole Verifier execution.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Set this to "true" to bypass only JAR creation.
	 */
	@Parameter(property = "spring.cloud.contract.verifier.jar.skip", defaultValue = "false")
	private boolean jarSkip;

	@Component
	private MavenProjectHelper projectHelper;

	/**
	 * Patterns that should not be taken into account for processing.
	 */
	@Parameter
	private String[] excludedFiles;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Component(role = Archiver.class, hint = "jar")
	private JarArchiver archiver;

	@Parameter(defaultValue = "stubs")
	private String classifier;

	/**
	 * If set to true then stubs jar is created only when stubs have changed since last
	 * build.
	 */
	@Parameter(property = "incrementalContractStubsJar", defaultValue = "true")
	private boolean incrementalContractStubsJar = true;

	@Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
	private MojoExecution mojoExecution;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession session;

	/**
	 * When enabled, this flag will tell stub runner to throw an exception when no stubs /
	 * contracts were found.
	 */
	@Parameter(property = "failOnNoContracts", defaultValue = "true")
	private boolean failOnNoContracts;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.skip || this.jarSkip) {
			getLog().info("Skipping Spring Cloud Contract Verifier execution: spring.cloud.contract.verifier.skip="
					+ this.skip + ", spring.cloud.contract.verifier.jar.skip=" + this.jarSkip);
			return;
		}
		else if (stubsOutputMissing(this.outputDirectory) && !this.failOnNoContracts) {
			getLog().warn(
					"The stubs output directory is missing, the flag to fail on no stubs if off - will continue without throwing an exception");
			return;
		}
		else if (stubsOutputMissing(this.outputDirectory) && this.failOnNoContracts) {
			throw new MojoExecutionException("Stubs could not be found: [" + this.outputDirectory.getAbsolutePath()
					+ "] .\nPlease make sure that spring-cloud-contract:convert was invoked");
		}
		File stubsJarFile = getStubJarDestFile();
		if (this.incrementalContractStubsJar && !inputFilesChangeDetected(outputDirectory, mojoExecution, session)) {
			getLog().info("Nothing to generate - stubs jar is up to date");
		}
		else {
			fillStubJar(this.outputDirectory, stubsJarFile);
		}
		this.projectHelper.attachArtifact(this.project, "jar", this.classifier, stubsJarFile);
	}

	private File getStubJarDestFile() {
		String stubArchiveName = this.projectFinalName + "-" + this.classifier + ".jar";
		return new File(this.projectBuildDirectory, stubArchiveName);
	}

	private void fillStubJar(File stubsOutputDir, File stubsJarFile) throws MojoFailureException {
		String[] excludes = excludes();
		getLog().info(
				"Files matching this pattern will be excluded from " + "stubs generation " + Arrays.toString(excludes));
		try {
			this.archiver.addDirectory(stubsOutputDir, new String[] { "**/*.*" },
					excludedFilesEmpty() ? new String[0] : this.excludedFiles);
			this.archiver.setCompress(true);
			this.archiver.setDestFile(stubsJarFile);
			this.archiver.addConfiguredManifest(ManifestCreator.createManifest(this.project));
			this.archiver.createArchive();
		}
		catch (Exception e) {
			throw new MojoFailureException("Exception while packaging " + this.classifier + " jar.", e);
		}
	}

	private boolean stubsOutputMissing(File stubsOutputDir) {
		return !stubsOutputDir.exists();
	}

	private String[] excludes() {
		List<String> excludes = new ArrayList<>();
		if (!excludedFilesEmpty()) {
			excludes.addAll(Arrays.asList(this.excludedFiles));
		}
		String[] array = new String[excludes.size()];
		array = excludes.toArray(array);
		return array;
	}

	private boolean excludedFilesEmpty() {
		return this.excludedFiles == null || this.excludedFiles.length == 0;
	}

}
