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

package org.springframework.cloud.contract.stubrunner.provider.moco;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubDownloader;
import org.springframework.cloud.contract.stubrunner.StubDownloaderBuilder;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptions;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Poor man's version of taking stubs from classpath. It needs much more
 * love and attention to go to the main sources.
 *
 * @author Marcin Grzejszczak
 */
public class ClasspathStubProvider implements StubDownloaderBuilder {

	private static final int TEMP_DIR_ATTEMPTS = 10000;

	@Override
	public StubDownloader build(StubRunnerOptions stubRunnerOptions) {
		final StubConfiguration configuration = DefaultGroovyMethods.first(stubRunnerOptions.getDependencies());
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				new DefaultResourceLoader());
		try {
			Resource[] resources = resolver.getResources("**/" + separatedArtifact(configuration) + "/**/*.json");
			final File tmp = createTempDir();
			tmp.deleteOnExit();
			// you'd have to write an impl to maintain the folder structure
			// this is just for demo
			for (Resource resource : resources) {
				Files.copy(resource.getInputStream(), new File(tmp, resource.getFile().getName()).toPath());
			}
			return new StubDownloader() {
				@Override
				public Map.Entry<StubConfiguration, File> downloadAndUnpackStubJar(
						StubConfiguration stubConfiguration) {
					return new AbstractMap.SimpleEntry<>(configuration, tmp);
				}
			};
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String separatedArtifact(StubConfiguration configuration) {
		return configuration.getGroupId().replace(".", File.separator) +
				File.separator + configuration.getArtifactId();
	}

	// Taken from Guava
	private File createTempDir() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		} throw new IllegalStateException(
				"Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to " + baseName + (
						TEMP_DIR_ATTEMPTS - 1) + ")");
	}
}