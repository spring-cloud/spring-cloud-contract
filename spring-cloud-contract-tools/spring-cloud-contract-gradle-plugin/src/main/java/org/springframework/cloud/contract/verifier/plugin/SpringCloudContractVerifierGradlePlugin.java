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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.*;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.springframework.cloud.contract.verifier.config.TestFramework;

import javax.inject.Inject;
import java.io.File;

/**
 * Gradle plugin for Spring Cloud Contract Verifier that from the DSL contract can
 * <ul>
 * <li>generate tests</li>
 * <li>generate stubs</li>
 * </ul>
 *
 * @author Jakub Kubrynski, codearte.io
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 1.0.0
 */
public class SpringCloudContractVerifierGradlePlugin implements Plugin<Project> {

	private static final Logger logger = Logging.getLogger(SpringCloudContractVerifierGradlePlugin.class);

	private static final String SPRING_CLOUD_VERSION = VersionExtractor.forClass(SpringCloudContractVerifierGradlePlugin.class);

	private static final String GROUP_NAME = "Verification";

	private static final String EXTENSION_NAME = "contracts";

	private static final String CONTRACT_TEST_SOURCE_SET_NAME = "contractTest";

	private static final String CONTRACT_TEST_COMPILE_ONLY_CONFIGURATION_NAME = "contractTestCompileOnly";

	private static final String CONTRACT_TEST_IMPLEMENTATION_CONFIGURATION_NAME = "contractTestImplementation";

	private static final String CONTRACT_TEST_RUNTIME_ONLY_CONFIGURATION_NAME = "contractTestRuntimeOnly";

	private static final String CONTRACT_TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "contractTestRuntimeClasspath";

	private static final String CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "contractTestGeneratorRuntimeClasspath";

	private static final String VERIFIER_STUBS_JAR_TASK_NAME = "verifierStubsJar";

	private static final String CONTRACT_TEST_TASK_NAME = "contractTest";

	private Project project;

	private final ProjectLayout layout;
	private final ProviderFactory providers;
	private final ObjectFactory objects;

	@Inject
	public SpringCloudContractVerifierGradlePlugin(
			final ProjectLayout layout,
			final ProviderFactory providers,
			final ObjectFactory objects
	) {
		this.layout = layout;
		this.providers = providers;
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		this.project = project;
		project.getPlugins().apply(JavaPlugin.class);
		ContractVerifierExtension extension = project.getExtensions().create(EXTENSION_NAME,
				ContractVerifierExtension.class);

		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		SourceSet contractTestSourceSet = configureSourceSets(extension, javaConvention);
		configureConfigurations();
		registerContractTestTask(contractTestSourceSet);

		TaskProvider<ContractsCopyTask> copyContracts = createAndConfigureCopyContractsTask(extension);
		TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs = createAndConfigureGenerateClientStubs(
				extension, copyContracts);

		createAndConfigureStubsJarTasks(extension, generateClientStubs);
		createGenerateTestsTask(extension, contractTestSourceSet, copyContracts);
		createAndConfigurePublishStubsToScmTask(extension, generateClientStubs);

		project.getDependencies().add(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME, "org.springframework.cloud:spring-cloud-contract-converters:" + SPRING_CLOUD_VERSION);

		project.afterEvaluate(inner -> {
			if (extension.getTestFramework().get() == TestFramework.SPOCK) {
				DirectoryProperty generatedTestSourcesDir = extension.getGeneratedTestGroovySourcesDir();
				if (generatedTestSourcesDir.isPresent()) {
					project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
						GroovySourceSet groovy = ((HasConvention) contractTestSourceSet).getConvention()
								.getPlugin(GroovySourceSet.class);
						groovy.getGroovy().srcDirs(generatedTestSourcesDir);
					});
				}
			} else {
				DirectoryProperty generatedTestSourcesDir = extension.getGeneratedTestJavaSourcesDir();
				if (generatedTestSourcesDir.isPresent()) {
					contractTestSourceSet.getJava().srcDirs(generatedTestSourcesDir);
				}
			}
		});
	}

	private SourceSet configureSourceSets(ContractVerifierExtension extension, JavaPluginConvention javaConvention) {
		SourceSetContainer sourceSets = javaConvention.getSourceSets();
		SourceSet contractTest = sourceSets.create(CONTRACT_TEST_SOURCE_SET_NAME);
		contractTest.getJava().srcDirs(extension.getGeneratedTestJavaSourcesDir());
		project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
			GroovySourceSet groovy = ((HasConvention) contractTest).getConvention().getPlugin(GroovySourceSet.class);
			groovy.getGroovy().srcDirs(extension.getGeneratedTestGroovySourcesDir());
		});
		contractTest.getResources().srcDirs(extension.getGeneratedTestResourcesDir());

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
		Configuration contractTestImplementation = configurations
				.getByName(CONTRACT_TEST_IMPLEMENTATION_CONFIGURATION_NAME);
		Configuration contractTestRuntimeOnly = configurations.getByName(CONTRACT_TEST_RUNTIME_ONLY_CONFIGURATION_NAME);

		contractTestCompileOnly.extendsFrom(testCompileOnly);
		contractTestImplementation.extendsFrom(testImplementation);
		contractTestRuntimeOnly.extendsFrom(testRuntimeOnly);

		configurations.create(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME, conf -> {
			conf.setVisible(false);
			conf.setCanBeResolved(true);
			conf.setCanBeConsumed(false);
			conf.attributes(attributes -> {
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
			});
			conf.extendsFrom(configurations.getByName(CONTRACT_TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME));
		});
	}

	private void registerContractTestTask(SourceSet contractTestSourceSet) {
		TaskProvider<Test> contractTestTask = project.getTasks().register(CONTRACT_TEST_TASK_NAME, Test.class,
				contractTest -> {
					contractTest.setDescription("Runs the contract tests");
					contractTest.setGroup(GROUP_NAME);
					contractTest.setTestClassesDirs(contractTestSourceSet.getOutput().getClassesDirs());
					contractTest.setClasspath(contractTestSourceSet.getRuntimeClasspath());

					contractTest.mustRunAfter(project.getTasks().named(JavaPlugin.TEST_TASK_NAME));
				});

		project.getTasks().named(JavaBasePlugin.CHECK_TASK_NAME, check -> check.dependsOn(contractTestTask));
	}

	private void createGenerateTestsTask(ContractVerifierExtension extension, SourceSet contractTestSourceSet,
			TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateServerTestsTask> task = project.getTasks().register(GenerateServerTestsTask.TASK_NAME,
				GenerateServerTestsTask.class);
		task.configure(generateServerTestsTask -> {
			generateServerTestsTask.setDescription("Generate server tests from the contracts");
			generateServerTestsTask.setGroup(GROUP_NAME);

			generateServerTestsTask.getContractsDslDir()
					.convention(copyContracts.flatMap(ContractsCopyTask::getCopiedContractsFolder));
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
			generateServerTestsTask.getBaseClassMappings()
					.convention(extension.getBaseClassMappings().getBaseClassMappings());
			generateServerTestsTask.getAssertJsonSize().convention(extension.getAssertJsonSize());
			generateServerTestsTask.getFailOnInProgress().convention(extension.getFailOnInProgress());
			generateServerTestsTask.getClasspath()
					.from(project.getConfigurations().getByName(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME));
			generateServerTestsTask.getGeneratedTestSourcesDir()
					.convention(extension.getTestFramework().flatMap(testFramework -> {
						Property<Directory> correctSourceSetDir;
						if (testFramework == TestFramework.SPOCK) {
							correctSourceSetDir = extension.getGeneratedTestGroovySourcesDir();
							return extension.getGeneratedTestGroovySourcesDir().orElse(correctSourceSetDir);
						}
						else {
							correctSourceSetDir = extension.getGeneratedTestJavaSourcesDir();
							return extension.getGeneratedTestJavaSourcesDir().orElse(correctSourceSetDir);
						}
					}));
			generateServerTestsTask.getGeneratedTestResourcesDir().convention(extension.getGeneratedTestResourcesDir());

			generateServerTestsTask.dependsOn(copyContracts);
		});
		project.getTasks().named(contractTestSourceSet.getProcessResourcesTaskName(), processContractTestResourcesTask -> {
			processContractTestResourcesTask.dependsOn(task);
		});
		project.getTasks().named(contractTestSourceSet.getCompileJavaTaskName(), compileContractTestJava -> compileContractTestJava.dependsOn(task));
		project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
			project.getTasks().named(contractTestSourceSet.getCompileTaskName("groovy"), compileContractTestGroovy -> {
				compileContractTestGroovy.dependsOn(task);
			});
		});
		project.getPlugins().withId("kotlin", kotlinPlugin -> {
			project.getTasks().named(contractTestSourceSet.getCompileTaskName("kotlin"), compileContractTestKotlin -> {
				compileContractTestKotlin.dependsOn(task);
			});
		});
		project.getPlugins().withId("org.jetbrains.kotlin.jvm", kotlinJvmPlugin -> {
			project.getTasks().named(contractTestSourceSet.getCompileTaskName("kotlin"), compileContractTestKotlin -> {
				compileContractTestKotlin.dependsOn(task);
			});
		});
	}

	private void createAndConfigurePublishStubsToScmTask(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<PublishStubsToScmTask> task = project.getTasks().register(PublishStubsToScmTask.TASK_NAME,
				PublishStubsToScmTask.class);
		task.configure(publishStubsToScmTask -> {
			publishStubsToScmTask
					.setDescription("The generated stubs get committed to the SCM repo and pushed to origin");
			publishStubsToScmTask.setGroup(GROUP_NAME);

			ContractVerifierExtension.ContractRepository stubs = extension.getPublishStubsToScm()
					.getContractRepository();
			ContractVerifierExtension.ContractRepository original = extension.getContractRepository();

			publishStubsToScmTask.getContractRepository().getRepositoryUrl()
					.convention(stubs.getRepositoryUrl().orElse(original.getRepositoryUrl()));
			publishStubsToScmTask.getContractRepository().getUsername()
					.convention(stubs.getUsername().orElse(original.getUsername()));
			publishStubsToScmTask.getContractRepository().getPassword()
					.convention(stubs.getPassword().orElse(original.getPassword()));
			publishStubsToScmTask.getContractRepository().getProxyHost()
					.convention(stubs.getProxyHost().orElse(original.getProxyHost()));
			publishStubsToScmTask.getContractRepository().getProxyPort()
					.convention(stubs.getProxyPort().orElse(original.getProxyPort()));
			publishStubsToScmTask.getContractsMode().convention(extension.getContractsMode());
			publishStubsToScmTask.getDeleteStubsAfterTest().convention(extension.getDeleteStubsAfterTest());
			publishStubsToScmTask.getFailOnNoContracts().convention(extension.getFailOnNoContracts());
			publishStubsToScmTask.getContractsProperties().convention(extension.getContractsProperties());
			publishStubsToScmTask.getStubsDir().convention(extension.getStubsOutputDir());
			publishStubsToScmTask.dependsOn(generateClientStubs);
		});
	}

	private TaskProvider<GenerateClientStubsFromDslTask> createAndConfigureGenerateClientStubs(
			ContractVerifierExtension extension, TaskProvider<ContractsCopyTask> copyContracts) {
		TaskProvider<GenerateClientStubsFromDslTask> task = project.getTasks().register(
				GenerateClientStubsFromDslTask.TASK_NAME, GenerateClientStubsFromDslTask.class, generateClientStubs -> {
					generateClientStubs.setGroup(GROUP_NAME);
					generateClientStubs.setDescription("Generate client stubs from the contracts");

					generateClientStubs.getContractsDslDir()
							.convention(copyContracts.flatMap(ContractsCopyTask::getCopiedContractsFolder));
					generateClientStubs.getExcludedFiles().convention(extension.getExcludedFiles());
					generateClientStubs.getExcludeBuildFolders().convention(extension.getExcludeBuildFolders());
					generateClientStubs.getClasspath()
							.from(project.getConfigurations().getByName(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME));

					generateClientStubs.getStubsOutputDir().convention(extension.getStubsOutputDir()
							.dir(buildRootPath(GenerateClientStubsFromDslTask.DEFAULT_MAPPINGS_FOLDER)));

					generateClientStubs.dependsOn(copyContracts);
				});
		return task;
	}

	private void createAndConfigureStubsJarTasks(ContractVerifierExtension extension,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Jar> verifierStubsJar = project.getTasks().register(VERIFIER_STUBS_JAR_TASK_NAME, Jar.class);
		verifierStubsJar.configure(stubsJar -> {
			stubsJar.setDescription("Creates the stubs JAR task");
			stubsJar.setGroup(GROUP_NAME);
			stubsJar.getArchiveBaseName().convention(providers.provider(project::getName));
			stubsJar.getArchiveClassifier().convention(extension.getStubsSuffix());
			stubsJar.from(extension.getStubsOutputDir());

			stubsJar.dependsOn(generateClientStubs);
		});
		project.artifacts(artifactHandler -> artifactHandler.add("archives", verifierStubsJar));
	}

	private TaskProvider<ContractsCopyTask> createAndConfigureCopyContractsTask(ContractVerifierExtension extension) {
		TaskProvider<ContractsCopyTask> task = project.getTasks().register(ContractsCopyTask.TASK_NAME,
				ContractsCopyTask.class, contractsCopyTask -> {
					contractsCopyTask.setGroup(GROUP_NAME);
					contractsCopyTask.setDescription("Copies contracts to the output folder");

					contractsCopyTask.getConvertToYaml().convention(extension.getConvertToYaml());
					contractsCopyTask.getFailOnNoContracts().convention(extension.getFailOnNoContracts());
					contractsCopyTask.getContractsDirectory()
							.convention(extension.getContractsDslDir().flatMap(contractsDslDir -> {
								return providers.provider(() -> {
									if (contractsDslDir.getAsFile().exists()) {
										return contractsDslDir;
									}
									else {
										Directory legacyContractsDslDir = layout.getProjectDirectory()
												.dir("src/test/resources/contracts");
										if (legacyContractsDslDir.getAsFile().exists()) {
											logger.warn(
													"Spring Cloud Contract Verifier Plugin: Locating contracts in <src/test/resources/contracts> has been removed. Please move them to <src/contractTest/resources/contracts>. This warning message will be removed in a future release.");
											return contractsDslDir;
										}
										else {
											return null;
										}
									}
								});
							}));
					contractsCopyTask.getContractDependency().getGroupId()
							.convention(extension.getContractDependency().getGroupId());
					contractsCopyTask.getContractDependency().getArtifactId()
							.convention(extension.getContractDependency().getArtifactId());
					contractsCopyTask.getContractDependency().getVersion()
							.convention(extension.getContractDependency().getVersion());
					contractsCopyTask.getContractDependency().getClassifier()
							.convention(extension.getContractDependency().getClassifier());
					contractsCopyTask.getContractDependency().getStringNotation()
							.convention(extension.getContractDependency().getStringNotation());
					contractsCopyTask.getContractRepository().getRepositoryUrl()
							.convention(extension.getContractRepository().getRepositoryUrl());
					contractsCopyTask.getContractRepository().getUsername()
							.convention(extension.getContractRepository().getUsername());
					contractsCopyTask.getContractRepository().getPassword()
							.convention(extension.getContractRepository().getPassword());
					contractsCopyTask.getContractRepository().getProxyHost()
							.convention(extension.getContractRepository().getProxyHost());
					contractsCopyTask.getContractRepository().getProxyPort()
							.convention(extension.getContractRepository().getProxyPort());
					contractsCopyTask.getContractsMode().convention(extension.getContractsMode());
					contractsCopyTask.getContractsProperties().convention(extension.getContractsProperties());
					contractsCopyTask.getContractsPath().convention(extension.getContractsPath());
					contractsCopyTask.getExcludeBuildFolders().convention(extension.getExcludeBuildFolders());
					contractsCopyTask.getDeleteStubsAfterTest().convention(extension.getDeleteStubsAfterTest());
					contractsCopyTask.getClasspath()
							.from(project.getConfigurations().getByName(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME));

					contractsCopyTask.getCopiedContractsFolder()
							.convention(extension.getStubsOutputDir().dir(buildRootPath(ContractsCopyTask.CONTRACTS)));
					contractsCopyTask.getBackupContractsFolder()
							.convention(extension.getStubsOutputDir().dir(buildRootPath(ContractsCopyTask.BACKUP)));
				});
		return task;
	}

	private Provider<String> buildRootPath(String path) {
		return providers.provider(() ->
				"META-INF"
					+ File.separator
					+ project.getGroup()
					+ File.separator
					+ project.getName()
					+ File.separator
					+ project.getVersion()
					+ File.separator
					+ path
		);
	}

}
