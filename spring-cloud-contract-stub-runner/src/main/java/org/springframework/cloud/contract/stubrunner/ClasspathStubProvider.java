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

package org.springframework.cloud.contract.stubrunner;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Stub downloader that picks stubs and contracts from the provided resource. If
 * {@link org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties#stubsMode}
 * is set to
 * {@link org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode#CLASSPATH}
 * then classpath is searched according to what has been passed in
 * {@link org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties#ids}.
 * The pattern to search for stubs looks like this
 *
 * <ul>
 * <li>{@code META-INF/group.id/artifactid/ ** /*.* }</li>
 * <li>{@code contracts/group.id/artifactid/ ** /*.* }</li>
 * <li>{@code mappings/group.id/artifactid/ ** /*.* }</li>
 * </ul>
 * <p>
 *
 * examples
 *
 * <p>
 * <ul>
 * <li>{@code META-INF/com.example/fooservice/1.0.0/ **}</li>
 * <li>{@code contracts/com.example/artifactid/ ** /*.* }</li>
 * <li>{@code mappings/com.example/artifactid/ ** /*.* }</li>
 * </ul>
 *
 * @author Marcin Grzejszczak
 * @since 1.1.1
 */
public class ClasspathStubProvider implements StubDownloaderBuilder {

	private static final Log log = LogFactory.getLog(ClasspathStubProvider.class);

	@Override
	public StubDownloader build(final StubRunnerOptions stubRunnerOptions) {
		if (stubRunnerOptions.stubsMode != StubRunnerProperties.StubsMode.CLASSPATH) {
			return null;
		}
		log.info("Will download stubs from classpath");
		return new ResourceResolvingStubDownloader(stubRunnerOptions, this::repoRoot,
				this::gavPattern);
	}

	private RepoRoots repoRoot(StubRunnerOptions stubRunnerOptions,
			StubConfiguration configuration) {
		Resource repositoryRoot = stubRunnerOptions.getStubRepositoryRoot();
		if (repositoryRoot instanceof ClassPathResource) {
			ClassPathResource classPathResource = (ClassPathResource) repositoryRoot;
			String path = classPathResource.getPath();
			if (StringUtils.hasText(path)) {
				return RepoRoots.asList(
						new RepoRoot(stubRunnerOptions.getStubRepositoryRootAsString()));
			}
		}
		String path = "/**/" + configuration.getGroupId() + "/"
				+ configuration.getArtifactId();
		return RepoRoots.asList(new RepoRoot("classpath*:/META-INF" + path, "/**/*.*"),
				new RepoRoot("classpath*:/contracts" + path, "/**/*.*"),
				new RepoRoot("classpath*:/mappings" + path, "/**/*.*"));
	}

	private Pattern gavPattern(StubConfiguration config) {
		String ga = config.getGroupId() + "." + config.getArtifactId();
		return Pattern.compile("^(.*)(" + ga + ")(.*)$");
	}

}
