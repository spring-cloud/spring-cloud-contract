/*
 *  Copyright 2013-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.contract.verifier.plugin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipException
import java.util.zip.ZipFile

import static java.nio.charset.StandardCharsets.UTF_8

abstract class ContractVerifierIntegrationSpec extends Specification {

	File testProjectDir

	def setup() {
		def dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
		def testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
		testFolder.mkdirs()
		testProjectDir = testFolder
	}

	public static final String SPOCK = "targetFramework = 'Spock'"
	public static final String JUNIT = "targetFramework = 'JUnit'"
	public static final String MVC_SPEC = "baseClassForTests = 'org.springframework.cloud.MvcSpec'"
	public static final String MVC_TEST = "baseClassForTests = 'org.springframework.cloud.MvcTest'"

	protected void setupForProject(String projectRoot) {
		copyResourcesToRoot(projectRoot)
		String gradlePluginSysProp = System.getProperty("contract-gradle-plugin-libs-dir")
		String gradlePluginLibsDir = (gradlePluginSysProp ?: new File("build/").absolutePath.toString()).replace('\\', '\\\\')
		String messagingLibDirProp = System.getProperty("messaging-libs-dir")
		String messagingLibDir = (messagingLibDirProp ?: new File("build/").absolutePath.toString()).replace('\\', '\\\\')

		buildFile.write """
			ext.messagingLibsDir = '$messagingLibDir'

			buildscript {
				dependencies {
					classpath fileTree(dir: '$gradlePluginLibsDir', include: '*.jar')
				}
			}

		""" + buildFile.text
		// Extending buildscript is required when 'apply' is used.
		// 'GradleRunner#withPluginClasspath' can be used when plugin is added using 'plugins { id...'
	}

	protected void switchToJunitTestFramework() {
		switchToJunitTestFramework(MVC_SPEC, MVC_TEST)
	}

	protected void switchToJunitTestFramework(String from, String to) {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll(SPOCK, JUNIT)
				.replaceAll(from, to)
		Files.write(path, content.getBytes(UTF_8))
	}

	protected void runTasksSuccessfully(String... tasks) {
		BuildResult result = run(tasks)
		result.tasks.each {
			assert it.outcome == TaskOutcome.SUCCESS || it.outcome == TaskOutcome.UP_TO_DATE
		}
	}

	protected BuildResult validateTasksOutcome(BuildResult result, TaskOutcome expectedOutcome, String... tasks) {
		tasks.each {
			BuildTask task = result.task(":" + it)
			assert task
			assert task.outcome == expectedOutcome
		}
		return result
	}

	protected BuildResult run(String... tasks) {
		return GradleRunner.create()
				.withProjectDir(testProjectDir)
				.withArguments(tasks)
				.forwardOutput()
				.build()
	}

	protected void copyResourcesToRoot(String srcDir) {
		copyResources(srcDir, testProjectDir)
	}

	protected void copyResources(String srcDir, File destinationFile) {
		ClassLoader classLoader = getClass().getClassLoader()
		URL resource = classLoader.getResource(srcDir)
		if (resource == null) {
			throw new RuntimeException("Could not find classpath resource: $srcDir")
		}
		File resourceFile = new File(resource.toURI())
		if (resourceFile.file) {
			Files.copy(resourceFile.toPath(), destinationFile.toPath())
		} else {
			Files.walkFileTree(resourceFile.toPath(),
					new CopyFileVisitor(destinationFile.toPath()))
		}
	}

	protected File file(String path) {
		return new File(testProjectDir, path)
	}

	protected boolean fileExists(String path) {
		return file(path).exists()
	}

	protected File getBuildFile() {
		return new File('build.gradle', testProjectDir)
	}

	protected boolean jarContainsContractVerifierContracts(String path) {
		assert fileExists(path)
		File rootFile = file(path)
		boolean containsGroovyFiles = false
		rootFile.eachFileRecurse { File file ->
			try {
				if (file.isFile() && file.name.endsWith('jar')) {
					new ZipFile(file).entries().each {
						if (it.name.endsWith('.groovy')) {
							containsGroovyFiles = true
						}
					}
				}
			}catch (ZipException zipEx) {
				println "Unable to open file ${file.name}"
			}
		}
		return containsGroovyFiles
	}

	private static class CopyFileVisitor extends SimpleFileVisitor<Path> {
		private final Path targetPath;
		private Path sourcePath = null
		public CopyFileVisitor(Path targetPath) {
			this.targetPath = targetPath
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path dir,
												 final BasicFileAttributes attrs) throws IOException {
			if (sourcePath == null) {
				sourcePath = dir
			} else {
				Files.createDirectories(targetPath.resolve(sourcePath
						.relativize(dir)))
			}
			return FileVisitResult.CONTINUE
		}

		@Override
		public FileVisitResult visitFile(final Path file,
										 final BasicFileAttributes attrs) throws IOException {
			Path target = targetPath.resolve(sourcePath.relativize(file))
			if (!target.toFile().exists()) {
				Files.copy(file, target)
			}
			return FileVisitResult.CONTINUE
		}
	}
}
