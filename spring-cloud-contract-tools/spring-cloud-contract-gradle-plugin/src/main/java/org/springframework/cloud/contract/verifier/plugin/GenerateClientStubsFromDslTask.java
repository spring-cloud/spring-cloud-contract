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

import javax.inject.Inject;

import org.eclipse.jgit.util.io.NullOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.springframework.util.StringUtils;

//TODO: Implement as an incremental task: https://gradle.org/docs/current/userguide/custom_tasks.html#incremental_tasks ?
/**
 * Generates stubs from the contracts.
 *
 * @author Marcin Grzejszczak
 * @author Anatoliy Balakirev
 * @author Shannon Pamperl
 * @since 2.0.0
 */
@CacheableTask
class GenerateClientStubsFromDslTask extends DefaultTask {

	static final String TASK_NAME = "generateClientStubs";
	static final String DEFAULT_MAPPINGS_FOLDER = "mappings";

	private final Property<Directory> contractsDslDir;

	private final ListProperty<String> excludedFiles;

	private final Property<Boolean> excludeBuildFolders;

	private final ConfigurableFileCollection classpath;

	private final DirectoryProperty stubsOutputDir;

	@Inject
	public GenerateClientStubsFromDslTask(ObjectFactory objects) {
		contractsDslDir = objects.directoryProperty();
		excludedFiles = objects.listProperty(String.class);
		excludeBuildFolders = objects.property(Boolean.class);
		classpath = objects.fileCollection();

		stubsOutputDir = objects.directoryProperty();
	}

	@TaskAction
	void generate() {
		File output = stubsOutputDir.get().getAsFile();
		getLogger().info("Stubs output dir [{}]", output);
		getLogger().info("Spring Cloud Contract Verifier Plugin: Invoking DSL to client stubs conversion");
		getLogger().info("Contracts dir is [{}] output stubs dir is [{}]", contractsDslDir.get().getAsFile(), output);
		OutputStream os;
		if (getLogger().isDebugEnabled()) {
			os = new ByteArrayOutputStream();
		} else {
			os = NullOutputStream.INSTANCE;
		}
		try {
			getProject().javaexec(exec -> {
				exec.getMainClass().set("org.springframework.cloud.contract.verifier.converter.RecursiveFilesConverterApplication");
				exec.classpath(classpath);
				exec.args(quoteAndEscape(output.getAbsolutePath()), quoteAndEscape(contractsDslDir.get().getAsFile().getAbsolutePath()),
						quoteAndEscape(StringUtils.collectionToCommaDelimitedString(excludedFiles.get())), quoteAndEscape(".*"), excludeBuildFolders.get());
				exec.setStandardOutput(os);
				exec.setErrorOutput(os);
			});
		} catch (Exception e) {
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
	public Property<Directory> getContractsDslDir() {
		return contractsDslDir;
	}

	@Input
	public ListProperty<String> getExcludedFiles() {
		return excludedFiles;
	}

	@Input
	public Property<Boolean> getExcludeBuildFolders() {
		return excludeBuildFolders;
	}

	@Classpath
	public ConfigurableFileCollection getClasspath() {
		return classpath;
	}

	@OutputDirectory
	public Property<Directory> getStubsOutputDir() {
		return stubsOutputDir;
	}

	// See: https://github.com/gradle/gradle/issues/6072
	private String quoteAndEscape(String str) {
		if (System.getProperty("os.name").contains("Windows")) {
			return "\"" + str.replace("\"", "\\\"") + "\"";
		}
		return str;
	}

}
