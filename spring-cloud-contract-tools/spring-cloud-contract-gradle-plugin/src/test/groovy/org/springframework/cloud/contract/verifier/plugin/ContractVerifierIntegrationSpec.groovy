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

package org.springframework.cloud.contract.verifier.plugin

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipException
import java.util.zip.ZipFile

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

import static java.nio.charset.StandardCharsets.UTF_8

abstract class ContractVerifierIntegrationSpec extends Specification {

	public static final String SPOCK = "testFramework = 'Spock'"
	public static final String JUNIT = "testFramework = 'JUnit'"
	public static final String MVC_SPEC = "'org.springframework.cloud.MvcSpec'"
	public static final String MVC_TEST = "'org.springframework.cloud.MvcTest'"
	protected static final boolean WORK_OFFLINE = Boolean.parseBoolean(System.getProperty('WORK_OFFLINE', 'false'))

	File testProjectDir

	def setup() {
		def dateString = new Date().format("yyyy-MM-dd_HH-mm-ss")
		def testFolder = new File("build/generated-tests/${getClass().simpleName}/${dateString}")
		testFolder.mkdirs()
		testProjectDir = testFolder
	}

	protected void setupForProject(String projectRoot) {
		copyResourcesToRoot(projectRoot)
	}

	protected void switchToJunitTestFramework() {
		switchToJunitTestFramework(MVC_SPEC, MVC_TEST)
	}

	protected void switchToJunitTestFramework(String from, String to) {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll(SPOCK, JUNIT).replaceAll(from, to)
		Files.write(path, content.getBytes(UTF_8))
	}

	protected void emptySourceSet() {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll("sourceSet = \"java\"", "")
		Files.write(path, content.getBytes(UTF_8))
	}

	protected BuildResult runTasksSuccessfully(String... tasks) {
		BuildResult result = run(tasks)
		result.tasks.each {
			assert it.outcome == TaskOutcome.SUCCESS || it.outcome == TaskOutcome.UP_TO_DATE || it.outcome == TaskOutcome.NO_SOURCE
		}
		return result
	}

	protected BuildResult validateTasksOutcome(BuildResult result, TaskOutcome expectedOutcome, String... tasks) {
		tasks.each {
			BuildTask task = result.task(":" + it)
			assert task
			assert task.outcome == expectedOutcome
		}
		return result
	}

	protected String[] checkAndPublishToMavenLocal() {
		String[] args = ["check", "publishToMavenLocal", "--info", "--stacktrace"] as String[]
		if (WORK_OFFLINE) {
			args << "--offline"
		}
		return args
	}

	protected BuildResult run(String... tasks) {
		return GradleRunner.create()
						   .withProjectDir(testProjectDir)
						   .withArguments(tasks)
						   .withPluginClasspath()
//						   .withDebug(true)
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
		}
		else {
			Files.walkFileTree(resourceFile.toPath(),
					new CopyFileVisitor(destinationFile.toPath()))
		}
	}

	protected File file(String path) {
		File file = new File(testProjectDir, path)
		println "Resolved path is [$file]"
		return file
	}

	protected boolean fileExists(String path) {
		return file(path).exists()
	}

	protected File getBuildFile() {
		return new File(testProjectDir, 'build.gradle')
	}

	protected boolean jarContainsContractVerifierContracts(String path) {
		assert fileExists(path)
		File rootFile = file(path)
		boolean containsGroovyFiles = false
		rootFile.eachFileRecurse { File file ->
			try {
				if (file.isFile() && file.name.endsWith('jar')) {
					ZipFile zipFile;
					try {
						zipFile = new ZipFile(file)
						zipFile.entries().each {
							if (it.name.endsWith('.groovy')) {
								containsGroovyFiles = true
							}
						}
					} finally {
						zipFile?.close()
					}
				}
			}
			catch (ZipException zipEx) {
				println "Unable to open file ${file.name}"
			}
		}
		return containsGroovyFiles
	}

	private static class CopyFileVisitor extends SimpleFileVisitor<Path> {
		private final Path targetPath
		private Path sourcePath = null

		CopyFileVisitor(Path targetPath) {
			this.targetPath = targetPath
		}

		@Override
		FileVisitResult preVisitDirectory(final Path dir,
				final BasicFileAttributes attrs) throws IOException {
			if (sourcePath == null) {
				sourcePath = dir
			}
			else {
				Files.createDirectories(targetPath.resolve(sourcePath
						.relativize(dir)))
			}
			return FileVisitResult.CONTINUE
		}

		@Override
		FileVisitResult visitFile(final Path file,
				final BasicFileAttributes attrs) throws IOException {
			Path target = targetPath.resolve(sourcePath.relativize(file))
			if (!target.toFile().exists()) {
				Files.copy(file, target)
			}
			return FileVisitResult.CONTINUE
		}
	}
}
