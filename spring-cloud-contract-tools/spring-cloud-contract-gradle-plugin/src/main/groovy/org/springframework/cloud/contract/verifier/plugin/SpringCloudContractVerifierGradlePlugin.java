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

import java.io.File;

import javax.annotation.Nullable;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.springframework.cloud.contract.verifier.config.TestFramework;

/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 *     <li>generate tests</li>
 *     <li>generate stubs</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 *
 * @since 1.0.0
 */
public class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final String GROUP_NAME = "Verification";
	private static final String EXTENSION_NAME = "contracts";
	private static final String CONTRACT_TEST_SOURCE_SET_NAME = "contractTest";
	private static final String CONTRACT_TEST_COMPILE_ONLY_CONFIGURATION_NAME = "contractTestCompileOnly";
	private static final String CONTRACT_TEST_IMPLEMENTATION_CONFIGURATION_NAME = "contractTestImplementation";
	private static final String CONTRACT_TEST_RUNTIME_ONLY_CONFIGURATION_NAME = "contractTestRuntimeOnly";
	private static final String VERIFIER_STUBS_JAR_TASK_NAME = "verifierStubsJar";
	private static final String CONTRACT_TEST_TASK_NAME = "contractTest";

	private Project project;

	@Override
	public void apply(Project project) {
		this.project = project;
		ContractVerifierExtension extension = project.getExtensions().create(EXTENSION_NAME, ContractVerifierExtension.class);

		JavaPluginConvention javaConvention = project.getConvention().getByType(JavaPluginConvention.class);
		SourceSet contractTestSourceSet = configureSourceSets(extension, javaConvention);
		configureConfigurations();
		registerContractTestTask(contractTestSourceSet);

		TaskProvider<ContractsCopyTask> copyContracts = createAndConfigureCopyContractsTask(extension);
		TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs = createAndConfigureGenerateClientStubs(extension, copyContracts);

		createAndConfigureStubsJarTasks(extension, copyContracts, generateClientStubs);
		createGenerateTestsTask(extension, contractTestSourceSet, copyContracts);
		createAndConfigurePublishStubsToScmTask(extension, generateClientStubs);
	}

	private SourceSet configureSourceSets(ContractVerifierExtension extension, JavaPluginConvention javaConvention) {
		Directory projectDir = project.getLayout().getProjectDirectory();
		SourceSetContainer sourceSets = javaConvention.getSourceSets();
		SourceSet contractTest = sourceSets.create(CONTRACT_TEST_SOURCE_SET_NAME);
		contractTest.getJava().srcDirs(projectDir.dir("src/contracts/java"), extension.getGeneratedTestJavaSourcesDir());
		project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
			GroovySourceSet groovy = ((HasConvention) contractTest).getConvention().getPlugin(GroovySourceSet.class);
			groovy.getGroovy().srcDirs(projectDir.dir("src/contracts/groovy"), extension.getGeneratedTestGroovySourcesDir());
		});
		contractTest.getResources().srcDirs(projectDir.dir("src/contracts/resources"), extension.getGeneratedTestResourcesDir());

		SourceSetOutput mainOutput = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput();
		SourceSetOutput testOutput = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).getOutput();

		FileCollection compileClasspath = contractTest.getCompileClasspath();
		contractTest.setCompileClasspath(compileClasspath.plus(mainOutput).plus(testOutput));

		FileCollection runtimeClasspath = contractTest.getRuntimeClasspath();
		contractTest.setRuntimeClasspath(runtimeClasspath.plus(mainOutput).plus(testOutput));
		return contractTest;
	}

	private void configureConfigurations() {
		ConfigurationContainer configurations = project.getConfigurations();

		Configuration testCompileOnly = configurations.getByName(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME);
		Configuration testImplementation = configurations.getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME);
		Configuration testRuntimeOnly = configurations.getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME);
		Configuration contractTestCompileOnly = configurations.getByName(CONTRACT_TEST_COMPILE_ONLY_CONFIGURATION_NAME);
		Configuration contractTestImplementation = configurations.getByName(CONTRACT_TEST_IMPLEMENTATION_CONFIGURATION_NAME);
		Configuration contractTestRuntimeOnly = configurations.getByName(CONTRACT_TEST_RUNTIME_ONLY_CONFIGURATION_NAME);

		contractTestCompileOnly.extendsFrom(testCompileOnly);
		contractTestImplementation.extendsFrom(testImplementation);
		contractTestRuntimeOnly.extendsFrom(testRuntimeOnly);
	}

	private void registerContractTestTask(SourceSet contractTestSourceSet) {
		TaskProvider<Test> contractTestTask = project.getTasks().register(CONTRACT_TEST_TASK_NAME, Test.class, contractTest -> {
			contractTest.setDescription("Runs the contract tests");
			contractTest.setGroup(GROUP_NAME);
			contractTest.setTestClassesDirs(contractTestSourceSet.getOutput().getClassesDirs());
			contractTest.setClasspath(contractTestSourceSet.getRuntimeClasspath());

			contractTest.mustRunAfter(project.getTasks().named(JavaPlugin.TEST_TASK_NAME));
		});

		project.getTasks().named(JavaBasePlugin.CHECK_TASK_NAME, check -> check.dependsOn(contractTestTask));
	}

	private void createGenerateTestsTask(ContractVerifierExtension extension, SourceSet contractTestSourceSet, TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateServerTestsTask> task = project.getTasks().register(GenerateServerTestsTask.TASK_NAME, GenerateServerTestsTask.class);
		task.configure(generateServerTestsTask -> {
			generateServerTestsTask.setDescription("Generate server tests from the contracts");
			generateServerTestsTask.setGroup(GROUP_NAME);
			// TODO: if there is a custom source set this won't be needed anymore
			generateServerTestsTask.setEnabled(!project.getGradle().getStartParameter().getExcludedTaskNames().contains("test"));

			generateServerTestsTask.getContractsDslDir().convention(copyContracts.flatMap(ContractsCopyTask::getCopiedContractsFolder));
			generateServerTestsTask.getNameSuffixForTests().convention(extension.getNameSuffixForTests());
			generateServerTestsTask.getBasePackageForTests().convention(extension.getBasePackageForTests());
			generateServerTestsTask.getBaseClassForTests().convention(extension.getBaseClassForTests());
			generateServerTestsTask.getPackageWithBaseClasses().convention(extension.getPackageWithBaseClasses());
			generateServerTestsTask.getExcludedFiles().convention(extension.getExcludedFiles());
			generateServerTestsTask.getIgnoredFiles().convention(extension.getIgnoredFiles());
			generateServerTestsTask.getIncludedFiles().convention(extension.getIncludedFiles());
			generateServerTestsTask.getImports().convention(extension.getImports());
			generateServerTestsTask.getStaticImports().convention(extension.getStaticImports());
			generateServerTestsTask.getTestMode().convention(extension.getTestMode());
			generateServerTestsTask.getTestFramework().convention(extension.getTestFramework());
			generateServerTestsTask.getBaseClassMappings().convention(extension.getBaseClassMappings().getBaseClassMappings());
			generateServerTestsTask.getAssertJsonSize().convention(extension.getAssertJsonSize());
			generateServerTestsTask.getFailOnInProgress().convention(extension.getFailOnInProgress());
			generateServerTestsTask.getGeneratedTestSourcesDir().convention(extension.getTestFramework().flatMap(testFramework -> {
				Property<Directory> correctSourceSetDir;
				if (testFramework == TestFramework.SPOCK) {
					correctSourceSetDir = extension.getGeneratedTestGroovySourcesDir();
				} else {
					correctSourceSetDir = extension.getGeneratedTestJavaSourcesDir();
				}
				return extension.getGeneratedTestSourcesDir().orElse(correctSourceSetDir);
			}));
			generateServerTestsTask.getGeneratedTestResourcesDir().convention(extension.getGeneratedTestResourcesDir());

			generateServerTestsTask.dependsOn(copyContracts);
		});
		project.getTasks().named(contractTestSourceSet.getCompileJavaTaskName(), compileContractTestJava -> {
			compileContractTestJava.dependsOn(task);
		});
		project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
			project.getTasks().named(contractTestSourceSet.getCompileTaskName("groovy"), compileContractTestGroovy -> {
				compileContractTestGroovy.dependsOn(task);
			});
		});
	}

	private void createAndConfigurePublishStubsToScmTask(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<PublishStubsToScmTask> task = project.getTasks().register(PublishStubsToScmTask.TASK_NAME, PublishStubsToScmTask.class);
		task.configure(publishStubsToScmTask -> {
			publishStubsToScmTask.setDescription("The generated stubs get committed to the SCM repo and pushed to origin");
			publishStubsToScmTask.setGroup(GROUP_NAME);

			ContractVerifierExtension.ContractRepository stubs = extension.getPublishStubsToScm().getContractRepository();
			ContractVerifierExtension.ContractRepository original = extension.getContractRepository();

			publishStubsToScmTask.getContractRepository().getRepositoryUrl().convention(stubs.getRepositoryUrl().orElse(original.getRepositoryUrl()));
			publishStubsToScmTask.getContractRepository().getUsername().convention(stubs.getUsername().orElse(original.getUsername()));
			publishStubsToScmTask.getContractRepository().getPassword().convention(stubs.getPassword().orElse(original.getPassword()));
			publishStubsToScmTask.getContractRepository().getProxyHost().convention(stubs.getProxyHost().orElse(original.getProxyHost()));
			publishStubsToScmTask.getContractRepository().getProxyPort().convention(stubs.getProxyPort().orElse(original.getProxyPort()));
			publishStubsToScmTask.getContractsMode().convention(extension.getContractsMode());
			publishStubsToScmTask.getDeleteStubsAfterTest().convention(extension.getDeleteStubsAfterTest());
			publishStubsToScmTask.getFailOnNoContracts().convention(extension.getFailOnNoContracts());
			publishStubsToScmTask.getContractsProperties().convention(extension.getContractsProperties());
			publishStubsToScmTask.getStubsDir().convention(generateClientStubs.flatMap(GenerateClientStubsFromDslTask::getStubsOutputDir));

			publishStubsToScmTask.dependsOn(generateClientStubs);
		});
	}

	private TaskProvider<GenerateClientStubsFromDslTask> createAndConfigureGenerateClientStubs(ContractVerifierExtension extension,
			TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateClientStubsFromDslTask> task = project.getTasks().register(GenerateClientStubsFromDslTask.TASK_NAME, GenerateClientStubsFromDslTask.class, generateClientStubs -> {
			generateClientStubs.setGroup(GROUP_NAME);
			generateClientStubs.setDescription("Generate client stubs from the contracts");

			generateClientStubs.getContractsDslDir().convention(copyContracts.flatMap(ContractsCopyTask::getCopiedContractsFolder));
			generateClientStubs.getExcludedFiles().convention(extension.getExcludedFiles());
			generateClientStubs.getExcludeBuildFolders().convention(extension.getExcludeBuildFolders());

			generateClientStubs.getStubsOutputDir().convention(extension.getStubsOutputDir().dir(buildRootPath() + File.separator + GenerateClientStubsFromDslTask.DEFAULT_MAPPINGS_FOLDER));

			generateClientStubs.dependsOn(copyContracts);
		});
		return task;
	}

	private void createAndConfigureStubsJarTasks(ContractVerifierExtension extension,
			TaskProvider<ContractsCopyTask> copyContracts,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Jar> task = stubsTask();
		if (task != null) {
			// How is this possible? Where can it come from?
			// TODO: This can only happen if one of the following is true:
			//   1) plugin was applied twice (allprojects/subprojects/standard apply)
			//   2) Another task with the same name exists, but was not provided by this plugin
			project.getLogger().info("Spring Cloud Contract Verifier Plugin: Stubs jar task was present - won't create one. Remember about adding it to artifacts as an archive!");
		}
		else {
			task = createStubsJarTask(extension, generateClientStubs);
		}
		task.configure(stubsJar -> stubsJar.dependsOn(copyContracts));
		createAndConfigureMavenPublishPlugin(task, extension);
	}

	@Deprecated
	private void createAndConfigureMavenPublishPlugin(TaskProvider<Jar> stubsTask, ContractVerifierExtension extension) {
		if (!classIsOnClasspath("org.gradle.api.publish.maven.plugins.MavenPublishPlugin")) {
			project.getLogger().debug("Maven Publish Plugin is not present - won't add default publication");
			return;
		}
		// This must be called within afterEvaluate due to getting data from extension, which must be initialised first:
		project.afterEvaluate(inner -> {
			project.getLogger().debug("Spring Cloud Contract Verifier Plugin: Generating default publication");
			if (extension.getDisableStubPublication().get()) {
				project.getLogger().info("You've switched off the stub publication - won't add default publication");
				return;
			}
			project.getPlugins().withType(MavenPublishPlugin.class, publishingPlugin -> {
				PublishingExtension publishingExtension = project.getExtensions().findByType(PublishingExtension.class);
				if (hasStubsPublication(publishingExtension)) {
					project.getLogger().info("Spring Cloud Contract Verifier Plugin: Stubs publication was present - won't create a new one. Remember about passing stubs as artifact");
				}
				else {
					project.getLogger().debug("Spring Cloud Contract Verifier Plugin: Stubs publication is not present - will create one");
					setPublications(publishingExtension, stubsTask);
				}
			});
		});
	}

	@Deprecated
	private void setPublications(PublishingExtension publishingExtension, TaskProvider<Jar> stubsTask) {
		project.getLogger().warn("Spring Cloud Contract Verifier Plugin: Creating stubs publication is deprecated");
		publishingExtension.publications(publicationsContainer -> {
			publicationsContainer.create("stubs", MavenPublication.class, stubsPublication -> {
				stubsPublication.setArtifactId(project.getName());
				stubsPublication.artifact(stubsTask);
			});
		});
	}

	private @Nullable TaskProvider<Jar> stubsTask() {
		try {
			return project.getTasks().named(VERIFIER_STUBS_JAR_TASK_NAME, Jar.class);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Deprecated
	private boolean hasStubsPublication(PublishingExtension publishingExtension) {
		try {
			return publishingExtension.getPublications().getByName("stubs") != null;
		}
		catch (Exception e) {
			return false;
		}
	}

	private TaskProvider<Jar> createStubsJarTask(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Jar> task = project.getTasks().register(VERIFIER_STUBS_JAR_TASK_NAME, Jar.class);
		task.configure(stubsJar -> {
			stubsJar.setDescription("Creates the stubs JAR task");
			stubsJar.setGroup(GROUP_NAME);
			stubsJar.getArchiveBaseName().convention(project.provider(project::getName));
			stubsJar.getArchiveClassifier().convention(extension.getStubsSuffix());
			stubsJar.from(generateClientStubs.flatMap(GenerateClientStubsFromDslTask::getStubsOutputDir));

			stubsJar.dependsOn(generateClientStubs);
		});
		project.artifacts(artifactHandler -> artifactHandler.add("archives", task));
		return task;
	}

	private TaskProvider<ContractsCopyTask> createAndConfigureCopyContractsTask(ContractVerifierExtension extension) {
		TaskProvider<ContractsCopyTask> task = project.getTasks().register(ContractsCopyTask.TASK_NAME, ContractsCopyTask.class, contractsCopyTask -> {
			contractsCopyTask.setGroup(GROUP_NAME);
			contractsCopyTask.setDescription("Copies contracts to the output folder");

			contractsCopyTask.getConvertToYaml().convention(extension.getConvertToYaml());
			contractsCopyTask.getFailOnNoContracts().convention(extension.getFailOnNoContracts());
			contractsCopyTask.getContractsDirectory().convention(extension.getContractsDslDir());
			contractsCopyTask.getContractDependency().getGroupId().convention(extension.getContractDependency().getGroupId());
			contractsCopyTask.getContractDependency().getArtifactId().convention(extension.getContractDependency().getArtifactId());
			contractsCopyTask.getContractDependency().getVersion().convention(extension.getContractDependency().getVersion());
			contractsCopyTask.getContractDependency().getClassifier().convention(extension.getContractDependency().getClassifier());
			contractsCopyTask.getContractDependency().getStringNotation().convention(extension.getContractDependency().getStringNotation());
			contractsCopyTask.getContractRepository().getRepositoryUrl().convention(extension.getContractRepository().getRepositoryUrl());
			contractsCopyTask.getContractRepository().getUsername().convention(extension.getContractRepository().getUsername());
			contractsCopyTask.getContractRepository().getPassword().convention(extension.getContractRepository().getPassword());
			contractsCopyTask.getContractRepository().getProxyHost().convention(extension.getContractRepository().getProxyHost());
			contractsCopyTask.getContractRepository().getProxyPort().convention(extension.getContractRepository().getProxyPort());
			contractsCopyTask.getContractsMode().convention(extension.getContractsMode());
			contractsCopyTask.getExcludeBuildFolders().convention(extension.getExcludeBuildFolders());
			contractsCopyTask.getDeleteStubsAfterTest().convention(extension.getDeleteStubsAfterTest());

			contractsCopyTask.getCopiedContractsFolder().convention(extension.getStubsOutputDir().dir(buildRootPath() + File.separator + ContractsCopyTask.CONTRACTS));
			contractsCopyTask.getBackupContractsFolder().convention(extension.getStubsOutputDir().dir(buildRootPath() + File.separator + ContractsCopyTask.BACKUP));
		});
		return task;
	}

	@Deprecated
	private boolean classIsOnClasspath(String className) {
		try {
			Class.forName(className);
			return true;
		}
		catch (Exception e) {
			project.getLogger().debug("Maven Publish Plugin is not available");
		}
		return false;
	}

	private String buildRootPath() {
		String groupId = project.getGroup().toString();
		String artifactId = project.getName();
		String version = project.getVersion().toString();
		return "META-INF/" + groupId + "/" + artifactId + "/" + version;
	}
}
