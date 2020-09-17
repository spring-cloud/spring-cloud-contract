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

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.BDDAssertions.then;

public class GitStubDownloaderTests {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	File temporaryFolder;

	@Before
	public void setup() throws Exception {
		this.temporaryFolder = this.tmp.newFolder();
		TestUtils.prepareLocalRepo();
		FileSystemUtils.copyRecursively(file("/git_samples/"), this.temporaryFolder);
	}

	@Test
	public void should_return_a_null_downloader_for_a_classptath_mode() {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();

		StubDownloader stubDownloader = stubDownloaderBuilder.build(new StubRunnerOptionsBuilder()
				.withStubsMode(StubRunnerProperties.StubsMode.CLASSPATH).withProperties(props()).build());

		then(stubDownloader).isNull();
	}

	@Test
	public void should_return_a_null_downloader_for_a_empty_repo() {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();

		StubDownloader stubDownloader = stubDownloaderBuilder.build(new StubRunnerOptionsBuilder()
				.withStubsMode(StubRunnerProperties.StubsMode.REMOTE).withProperties(props()).build());

		then(stubDownloader).isNull();
	}

	@Test
	public void should_return_a_null_downloader_for_a_non_git_repo() {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();

		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("http://www.foo.com/").withProperties(props()).build());

		then(stubDownloader).isNull();
	}

	@Test
	public void should_pick_stubs_for_group_and_artifact_with_version_from_a_git_repo() throws Exception {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-git/").getAbsolutePath() + "/")
				.replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("foo.bar:bazService:0.0.1-SNAPSHOT"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("foo.bar" + File.separator + "bazService" + File.separator + "0.0.1-SNAPSHOT");
	}

	@Test
	public void should_pick_latest_build_snapshot_stubs_when_latest_version_set() throws URISyntaxException {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-git/").getAbsolutePath() + "/")
				.replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:+"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains("com.example" + File.separator + "beer-api-producer-external"
				+ File.separator + "1.0.0.BUILD-SNAPSHOT");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:latest"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains("com.example" + File.separator + "beer-api-producer-external"
				+ File.separator + "1.0.0.BUILD-SNAPSHOT");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:LATEST"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains("com.example" + File.separator + "beer-api-producer-external"
				+ File.separator + "1.0.0.BUILD-SNAPSHOT");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.issue1305:beer-api-producer-external:+"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains(
				"com.issue1305" + File.separator + "beer-api-producer-external" + File.separator + "0.0.11-SNAPSHOT");
	}

	@Test
	public void should_pick_latest_release_stubs_when_release_version_set() throws URISyntaxException {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-git/").getAbsolutePath() + "/")
				.replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:release"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains(
				"com.example" + File.separator + "beer-api-producer-external" + File.separator + "1.0.0.RELEASE");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:RELEASE"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath()).contains(
				"com.example" + File.separator + "beer-api-producer-external" + File.separator + "1.0.0.RELEASE");
	}

	@Test
	public void should_pick_latest_build_snapshot_stubs_when_latest_version_set_and_latest_folder_exists()
			throws URISyntaxException {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-predefined-names-git/").getAbsolutePath()
				.replace("/", File.separator) + "/").replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:+"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("com.example" + File.separator + "beer-api-producer-external" + File.separator + "latest");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:latest"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("com.example" + File.separator + "beer-api-producer-external" + File.separator + "latest");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:LATEST"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("com.example" + File.separator + "beer-api-producer-external" + File.separator + "latest");
	}

	@Test
	public void should_pick_release_folder_when_release_version_set() throws URISyntaxException {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-predefined-names-git/").getAbsolutePath() + "/")
				.replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		Map.Entry<StubConfiguration, File> entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:release"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("com.example" + File.separator + "beer-api-producer-external" + File.separator + "release");

		entry = stubDownloader
				.downloadAndUnpackStubJar(new StubConfiguration("com.example:beer-api-producer-external:RELEASE"));

		then(entry).isNotNull();
		then(entry.getValue().getAbsolutePath())
				.contains("com.example" + File.separator + "beer-api-producer-external" + File.separator + "release");
	}

	@Test
	public void should_fail_to_fetch_stubs_when_concrete_version_was_not_specified() throws URISyntaxException {
		StubDownloaderBuilder stubDownloaderBuilder = new ScmStubDownloaderBuilder();
		String contractFolderLocation = (file("/git_samples/contract-git/").getAbsolutePath() + "/")
				.replace(File.separator, "/");
		StubDownloader stubDownloader = stubDownloaderBuilder
				.build(new StubRunnerOptionsBuilder().withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
						.withStubRepositoryRoot("git://" + contractFolderLocation).withProperties(props()).build());

		try {
			stubDownloader.downloadAndUnpackStubJar(new StubConfiguration("foo.bar", "bazService", ""));
		}
		catch (IllegalStateException e) {
			then(e).hasMessageContaining("Concrete version wasn't passed for [foo.bar:bazService::stubs]");
		}
	}

	private Map<String, String> props() {
		Map<String, String> map = new HashMap<>();
		map.put("git.branch", "master");
		return map;
	}

	private File file(String relativePath) throws URISyntaxException {
		return new File(GitStubDownloaderTests.class.getResource(relativePath).toURI());
	}

}
