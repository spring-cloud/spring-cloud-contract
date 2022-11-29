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

package org.springframework.cloud.contract.verifier.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jgit.util.io.NullOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.config.TestMode;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Task used to generate server side tests
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 1.0.0
 */
@CacheableTask
class GenerateServerTestsTask extends DefaultTask {

	static final String TASK_NAME = "generateContractTests";

	private final DirectoryProperty contractsDslDir;

	private final Property<String> nameSuffixForTests;

	private final Property<String> basePackageForTests;

	private final Property<String> baseClassForTests;

	private final Property<String> packageWithBaseClasses;

	private final ListProperty<String> excludedFiles;

	private final ListProperty<String> ignoredFiles;

	private final ListProperty<String> includedFiles;

	private final ListProperty<String> imports;

	private final ListProperty<String> staticImports;

	private final Property<TestMode> testMode;

	private final Property<TestFramework> testFramework;

	private final MapProperty<String, String> baseClassMappings;

	private final Property<Boolean> assertJsonSize;

	private final Property<Boolean> failOnInProgress;

	private final ConfigurableFileCollection classpath;

	private final DirectoryProperty generatedTestSourcesDir;

	private final DirectoryProperty generatedTestResourcesDir;

	final ExecOperations executors;

	@Inject
	public GenerateServerTestsTask(
			final ObjectFactory objects,
			final ExecOperations executors
	) {
		this.executors = executors;

		this.contractsDslDir = objects.directoryProperty();
		this.nameSuffixForTests = objects.property(String.class);
		this.basePackageForTests = objects.property(String.class);
		this.baseClassForTests = objects.property(String.class);
		this.packageWithBaseClasses = objects.property(String.class);
		this.excludedFiles = objects.listProperty(String.class);
		this.ignoredFiles = objects.listProperty(String.class);
		this.includedFiles = objects.listProperty(String.class);
		this.imports = objects.listProperty(String.class);
		this.staticImports = objects.listProperty(String.class);
		this.testMode = objects.property(TestMode.class);
		this.testFramework = objects.property(TestFramework.class);
		this.baseClassMappings = objects.mapProperty(String.class, String.class);
		this.assertJsonSize = objects.property(Boolean.class);
		this.failOnInProgress = objects.property(Boolean.class);
		this.classpath = objects.fileCollection();
		this.generatedTestSourcesDir = objects.directoryProperty();
		this.generatedTestResourcesDir = objects.directoryProperty();
	}

	@TaskAction
	void generate() {
		File generatedTestSources = this.generatedTestSourcesDir.get().getAsFile();
		File generatedTestResources = this.generatedTestResourcesDir.get().getAsFile();
		getLogger().info("Generated test sources dir [{}]", generatedTestSources);
		getLogger().info("Generated test resources dir [{}]", generatedTestResources);
		File contractsDslDir = this.contractsDslDir.get().getAsFile();
		String includedContracts = ".*";
		getLogger().info("Spring Cloud Contract Verifier Plugin: Invoking test sources generation");
		getLogger().info("Contracts are unpacked to [{}]", contractsDslDir);
		getLogger().info("Included contracts are [{}]", includedContracts);
		ContractVerifierConfigProperties properties = toConfigProperties(contractsDslDir, includedContracts, generatedTestSources, generatedTestResources);
		OutputStream os;
		if (getLogger().isDebugEnabled()) {
			os = new ByteArrayOutputStream();
		} else {
			os = NullOutputStream.INSTANCE;
		}
		try {
			String propertiesJson = new ObjectMapper().writeValueAsString(properties);
			executors.javaexec(exec -> {
				exec.getMainClass().set("org.springframework.cloud.contract.verifier.TestGeneratorApplication");
				exec.classpath(classpath);
				exec.args(quoteAndEscape(propertiesJson));
				exec.setStandardOutput(os);
				exec.setErrorOutput(os);
			});
		}
		catch (Exception e) {
			throw new GradleException("Spring Cloud Contract Verifier Plugin exception: " + e.getMessage(), e);
		} finally {
			if (getLogger().isDebugEnabled()) {
				getLogger().debug(os.toString());
			}
		}
	}

	@InputDirectory
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.RELATIVE)
	DirectoryProperty getContractsDslDir() {
		return contractsDslDir;
	}

	@Input
	@Optional
	Property<String> getNameSuffixForTests() {
		return nameSuffixForTests;
	}

	@Input
	@Optional
	Property<String> getBasePackageForTests() {
		return basePackageForTests;
	}

	@Input
	@Optional
	Property<String> getBaseClassForTests() {
		return baseClassForTests;
	}

	@Input
	@Optional
	Property<String> getPackageWithBaseClasses() {
		return packageWithBaseClasses;
	}

	@Input
	ListProperty<String> getExcludedFiles() {
		return excludedFiles;
	}

	@Input
	ListProperty<String> getIgnoredFiles() {
		return ignoredFiles;
	}

	@Input
	ListProperty<String> getIncludedFiles() {
		return includedFiles;
	}

	@Input
	ListProperty<String> getImports() {
		return imports;
	}

	@Input
	ListProperty<String> getStaticImports() {
		return staticImports;
	}

	@Input
	Property<TestMode> getTestMode() {
		return testMode;
	}

	@Input
	Property<TestFramework> getTestFramework() {
		return testFramework;
	}

	@Input
	MapProperty<String, String> getBaseClassMappings() {
		return baseClassMappings;
	}

	@Input
	Property<Boolean> getAssertJsonSize() {
		return assertJsonSize;
	}

	@Input
	Property<Boolean> getFailOnInProgress() {
		return failOnInProgress;
	}

	@Classpath
	ConfigurableFileCollection getClasspath() {
		return classpath;
	}

	@OutputDirectory
	DirectoryProperty getGeneratedTestSourcesDir() {
		return generatedTestSourcesDir;
	}

	@OutputDirectory
	DirectoryProperty getGeneratedTestResourcesDir() {
		return generatedTestResourcesDir;
	}

	private ContractVerifierConfigProperties toConfigProperties(File contractsDslDir, String includedContracts,
			File generatedTestSources, File generatedTestResources) {
		List<String> excludedFiles = this.excludedFiles.get();
		List<String> ignoredFiles = this.ignoredFiles.get();
		List<String> includedFiles = this.includedFiles.get();
		String[] imports = this.imports.get().toArray(new String[0]);
		String[] staticImports = this.staticImports.get().toArray(new String[0]);

		ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties();
		properties.setIncludedContracts(includedContracts);
		properties.setContractsDslDir(contractsDslDir);
		properties.setNameSuffixForTests(nameSuffixForTests.getOrNull());
		properties.setGeneratedTestSourcesDir(generatedTestSources);
		properties.setGeneratedTestResourcesDir(generatedTestResources);
		properties.setBasePackageForTests(basePackageForTests.getOrNull());
		properties.setBaseClassForTests(baseClassForTests.getOrNull());
		properties.setPackageWithBaseClasses(packageWithBaseClasses.getOrNull());
		properties.setExcludedFiles(excludedFiles);
		properties.setIgnoredFiles(ignoredFiles);
		properties.setIncludedFiles(includedFiles);
		properties.setImports(imports);
		properties.setStaticImports(staticImports);
		properties.setTestMode(testMode.get());
		properties.setTestFramework(testFramework.get());
		properties.setBaseClassMappings(baseClassMappings.get());
		properties.setAssertJsonSize(assertJsonSize.get());
		properties.setFailOnInProgress(failOnInProgress.get());
		return properties;
	}

	// See: https://github.com/gradle/gradle/issues/6072
	private String quoteAndEscape(String str) {
		if (System.getProperty("os.name").contains("Windows")) {
			return "\"" + str.replace("\"", "\\\"") + "\"";
		}
		return str;
	}

}
