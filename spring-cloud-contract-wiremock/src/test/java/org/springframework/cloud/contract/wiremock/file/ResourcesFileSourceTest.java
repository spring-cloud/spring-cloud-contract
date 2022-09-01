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

package org.springframework.cloud.contract.wiremock.file;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourcesFileSourceTest {

	@DisplayName("find all files in the multiple files directories.")
	@Test
	void child() {
		// given
		Resource resource1 = new ClassPathResource("src/test/resources/files_banner");
		Resource resource2 = new ClassPathResource("src/test/resources/files_notice");
		ResourcesFileSource resourcesFileSource = new ResourcesFileSource(resource1, resource2);

		// when
		String filesDirName = "__files";
		FileSource fileSource = resourcesFileSource.child(filesDirName);

		// then
		assertThat(fileSource).isInstanceOf(ResourcesFileSource.class);
		assertThat(fileSource.listFilesRecursively()).hasSize(4);
	}

	@DisplayName("find a mapped response file in multiple directories.")
	@Test
	void getBinaryFileNamed() {
		// given
		Resource resource1 = new ClassPathResource("src/test/resources/files_banner/__files");
		Resource resource2 = new ClassPathResource("src/test/resources/files_notice/__files");
		ResourcesFileSource resourcesFileSource = new ResourcesFileSource(resource1, resource2);

		// when & then
		assertThat(resourcesFileSource.getBinaryFileNamed("response-bannerList-success.json").getStream()).isNotEmpty();
		assertThat(resourcesFileSource.getBinaryFileNamed("response-noticeList-success.json").getStream()).isNotEmpty();
	}

	@DisplayName("find a stub file in jar")
	@Test
	void find_stub_file_in_jar() throws URISyntaxException {

		// given
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("jarfiles/module-banner-test-fixtures.jar").getFile());

		String bannerStubPathInJar = "jar:file:" + file.getAbsolutePath() + "!/wiremock/banner/__files";
		String bannerStubFilename = "response-bannerList-success.json";
		ClasspathFileSource classpathFileSource1 = mock(ClasspathFileSource.class);
		when(classpathFileSource1.getUri()).thenReturn(new URI(bannerStubPathInJar));
		when(classpathFileSource1.exists()).thenReturn(true);
		when(classpathFileSource1.getBinaryFileNamed(bannerStubFilename))
				.thenReturn(new BinaryFile(new URI(bannerStubPathInJar + "/response-bannerList-success.json")));
		ResourcesFileSource resourcesFileSource = new ResourcesFileSource(classpathFileSource1);

		// when & then
		assertThat(resourcesFileSource.getBinaryFileNamed(bannerStubFilename).getStream()).isNotEmpty();
	}

}
