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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JvmTestSuitePlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.GroovySourceDirectorySet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.base.TestingExtension;
import org.gradle.util.GradleVersion;
import org.springframework.cloud.contract.verifier.config.TestFramework;

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
		project.getPlugins().apply(JvmTestSuitePlugin.class);
		ContractVerifierExtension extension = project.getExtensions().create(EXTENSION_NAME,
				ContractVerifierExtension.class);

		TaskProvider<ContractsCopyTask> copyContracts = registerCopyContractsTask(extension);
		TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs = registerGenerateClientStubsTask(
				extension, copyContracts);

		registerStubsJarTask(extension, copyContracts, generateClientStubs);
		TaskProvider<GenerateServerTestsTask> generateServerTestsTaskProvider =
				registerGenerateServerTestsTask(extension, copyContracts);
		registerPublishStubsToScmTask(extension, generateClientStubs);


		JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
		TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
		testing.getSuites().register("contractTest", JvmTestSuite.class, contractTestSuite -> {
			contractTestSuite.useJUnitJupiter();

			contractTestSuite.dependencies(dependencies -> {
				if (GradleVersion.current().compareTo(GradleVersion.version("7.6")) < 0) {
					try {
						Method implementation = dependencies.getClass().getMethod("implementation", Object.class);
						implementation.invoke(dependencies, project);
					} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
						throw new RuntimeException("Unable to add project dependencies", e);
					}
				} else {
					dependencies.getImplementation().add(dependencies.project());
				}
			});

			contractTestSuite.sources(sourceSet -> {
				configureSourceSets(extension, javaExtension, sourceSet);

				project.getTasks().named(sourceSet.getProcessResourcesTaskName(), processContractTestResourcesTask -> {
					processContractTestResourcesTask.dependsOn(generateServerTestsTaskProvider);
				});
				project.getTasks().named(sourceSet.getCompileJavaTaskName(), compileContractTestJava -> compileContractTestJava.dependsOn(generateServerTestsTaskProvider));
				project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
					project.getTasks().named(sourceSet.getCompileTaskName("groovy"), compileContractTestGroovy -> {
						compileContractTestGroovy.dependsOn(generateServerTestsTaskProvider);
					});
				});
				project.getPlugins().withId("kotlin", kotlinPlugin -> {
					project.getTasks().named(sourceSet.getCompileTaskName("kotlin"), compileContractTestKotlin -> {
						compileContractTestKotlin.dependsOn(generateServerTestsTaskProvider);
					});
				});
				project.getPlugins().withId("org.jetbrains.kotlin.jvm", kotlinJvmPlugin -> {
					project.getTasks().named(sourceSet.getCompileTaskName("kotlin"), compileContractTestKotlin -> {
						compileContractTestKotlin.dependsOn(generateServerTestsTaskProvider);
					});
				});
			});

			contractTestSuite.getTargets().all(testSuiteTarget -> configureTestTask(testSuiteTarget.getTestTask()));
		});

		configureConfigurations();

		project.getDependencies().add(CONTRACT_TEST_GENERATOR_RUNTIME_CLASSPATH_CONFIGURATION_NAME, "org.springframework.cloud:spring-cloud-contract-converters:" + SPRING_CLOUD_VERSION);
	}

	private SourceSet configureSourceSets(ContractVerifierExtension extension, JavaPluginExtension javaExtension, SourceSet contractTest) {
		SourceSetContainer sourceSets = javaExtension.getSourceSets();
		ConfigurationContainer configurations = project.getConfigurations();
		ObjectFactory objects = project.getObjects();
		contractTest.getJava().srcDirs(extension.getGeneratedTestJavaSourcesDir());
		project.getPlugins().withType(GroovyPlugin.class, groovyPlugin -> {
			GroovySourceDirectorySet groovy = contractTest.getExtensions().getByType(GroovySourceDirectorySet.class);
			groovy.srcDirs(extension.getGeneratedTestGroovySourcesDir());
		});
		contractTest.getResources().srcDirs(extension.getGeneratedTestResourcesDir());

		SourceSetOutput mainOutput = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput();
		SourceSetOutput testOutput = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME).getOutput();

		Configuration contractTestCompileClasspathConfiguration = configurations.getByName(contractTest.getCompileClasspathConfigurationName());
		Configuration contractTestRuntimeClasspathConfiguration = configurations.getByName(contractTest.getRuntimeClasspathConfigurationName());

		contractTest.setCompileClasspath(objects.fileCollection().from(testOutput, mainOutput, contractTestCompileClasspathConfiguration));
		contractTest.setRuntimeClasspath(objects.fileCollection().from(contractTest.getOutput(), testOutput, mainOutput, contractTestRuntimeClasspathConfiguration));
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

	private void configureTestTask(TaskProvider<Test> contractTestTaskProvider) {
		contractTestTaskProvider.configure(contractTest -> {
			contractTest.setDescription("Runs the contract tests");
			contractTest.setGroup(GROUP_NAME);

			contractTest.shouldRunAfter(project.getTasks().named(JavaPlugin.TEST_TASK_NAME));
		});

		project.getTasks().named(JavaBasePlugin.CHECK_TASK_NAME, check -> check.dependsOn(contractTestTaskProvider));
	}

	private TaskProvider<GenerateServerTestsTask> registerGenerateServerTestsTask(ContractVerifierExtension extension,
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
		return task;
	}

	private void registerPublishStubsToScmTask(ContractVerifierExtension extension,
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

	private TaskProvider<GenerateClientStubsFromDslTask> registerGenerateClientStubsTask(
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

	private void registerStubsJarTask(ContractVerifierExtension extension,
			TaskProvider<ContractsCopyTask> copyContracts,
			TaskProvider<GenerateClientStubsFromDslTask> generateClientStubs) {
		TaskProvider<Jar> verifierStubsJar = project.getTasks().register(VERIFIER_STUBS_JAR_TASK_NAME, Jar.class);
		verifierStubsJar.configure(stubsJar -> {
			stubsJar.setDescription("Creates the stubs JAR task");
			stubsJar.setGroup(GROUP_NAME);
			stubsJar.getArchiveBaseName().convention(providers.provider(project::getName));
			stubsJar.getArchiveClassifier().convention(extension.getStubsSuffix());
			stubsJar.from(extension.getStubsOutputDir());

			stubsJar.dependsOn(copyContracts);
			stubsJar.dependsOn(generateClientStubs);
		});
		project.artifacts(artifactHandler -> artifactHandler.add("archives", verifierStubsJar));
	}

	private TaskProvider<ContractsCopyTask> registerCopyContractsTask(ContractVerifierExtension extension) {
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
