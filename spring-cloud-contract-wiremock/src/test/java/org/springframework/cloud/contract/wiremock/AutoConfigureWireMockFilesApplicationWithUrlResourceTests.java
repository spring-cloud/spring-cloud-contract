/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.cloud.contract.wiremock;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AutoConfigureWireMockFilesApplicationWithUrlResourceTests.Config.class,
		properties = "app.baseUrl=http://localhost:${wiremock.server.port}",
		webEnvironment = WebEnvironment.NONE)
// tag::load_all_stubs[]
@AutoConfigureWireMock(port = 0, stubs = "classpath*:/META-INF/**/mappings/**/*.json")
// end::load_all_stubs[]
public class AutoConfigureWireMockFilesApplicationWithUrlResourceTests {

	@Autowired
	private RestTemplateSaganClient client;

	@Test
	public void contextLoads() throws Exception {
		Release release = this.client.getRelease("spring-framework", "5.0.0.RC4");

		then(release.releaseStatus).isEqualTo("PRERELEASE");
		then(release.refDocUrl).isEqualTo("https://docs.spring.io/spring/docs/{version}/spring-framework-reference/");
		then(release.apiDocUrl).isEqualTo("https://docs.spring.io/spring/docs/{version}/javadoc-api/");
		then(release.groupId).isEqualTo("org.springframework");
		then(release.artifactId).isEqualTo("spring-context");
		then(release.repository.id).isEqualTo("spring-milestones");
		then(release.repository.name).isEqualTo("Spring Milestones");
		then(release.repository.url).isEqualTo("https://repo.spring.io/libs-milestone");
		then(release.repository.snapshotsEnabled).isFalse();
		then(release.version).isEqualTo("5.0.0.RC4");
		then(release.current).isFalse();
		then(release.generalAvailability).isFalse();
		then(release.preRelease).isTrue();
		then(release.versionDisplayName).isEqualTo("5.0.0 RC4");
		then(release.snapshot).isFalse();
	}

	@Configuration
	@EnableAutoConfiguration
	static class Config {
		@Bean
		RestTemplateSaganClient restTemplateSaganClient(@Value("${app.baseUrl}") String url) {
			return new RestTemplateSaganClient(new RestTemplate(), url);
		}
	}

}

/**
 * @author Marcin Grzejszczak
 */
class RestTemplateSaganClient {

	private final RestTemplate restTemplate;
	private final String baseUrl;

	RestTemplateSaganClient(RestTemplate restTemplate, String url) {
		this.restTemplate = restTemplate;
		this.baseUrl = url;
	}

	public Release getRelease(String projectName, String releaseVersion) {
		return this.restTemplate.getForObject(this.baseUrl + "/project_metadata/{projectName}/releases/{releaseVersion}", Release.class, projectName, releaseVersion);
	}
}

/**
 * @author Marcin Grzejszczak
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
class Project {
	public String id = "";
	public String name = "";
	public String repoUrl = "";
	public String siteUrl = "";
	public String category = "";
	public String stackOverflowTags;
	public List<Release> projectReleases = new ArrayList<>();
	public List<String> stackOverflowTagList = new ArrayList<>();
	public Boolean aggregator;

	@Override public String toString() {
		return "Project{" + "id='" + this.id + '\'' + ", name='" + this.name + '\'' + ", repoUrl='"
				+ this.repoUrl + '\'' + ", siteUrl='" + this.siteUrl + '\'' + ", category='"
				+ this.category + '\'' + ", stackOverflowTags='" + this.stackOverflowTags + '\''
				+ ", projectReleases=" + this.projectReleases + ", stackOverflowTagList="
				+ this.stackOverflowTagList + ", aggregator=" + this.aggregator + '}';
	}
}

/**
 * @author Marcin Grzejszczak
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
class Release {
	public String releaseStatus = "";
	public String refDocUrl = "";
	public String apiDocUrl = "";
	public String groupId = "";
	public String artifactId = "";
	public Repository repository;
	public String version = "";
	public boolean current;
	public boolean generalAvailability;
	public boolean preRelease;
	public String versionDisplayName = "";
	public boolean snapshot;

	@Override public String toString() {
		return "Release{" + "releaseStatus='" + this.releaseStatus + '\'' + ", refDocUrl='"
				+ this.refDocUrl + '\'' + ", apiDocUrl='" + this.apiDocUrl + '\'' + ", groupId='"
				+ this.groupId + '\'' + ", artifactId='" + this.artifactId + '\'' + ", repository="
				+ this.repository + ", version='" + this.version + '\'' + ", current=" + this.current
				+ ", generalAvailability=" + this.generalAvailability + ", preRelease="
				+ this.preRelease + ", versionDisplayName='" + this.versionDisplayName + '\''
				+ ", snapshot=" + this.snapshot + '}';
	}
}

/**
 * @author Marcin Grzejszczak
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
class ReleaseUpdate {
	public String groupId = "";
	public String artifactId = "";
	public String version = "";
	public String releaseStatus = "";
	public String refDocUrl = "";
	public String apiDocUrl = "";
	public Boolean current;
	public Repository repository;

	@Override public String toString() {
		return "ReleaseUpdate{" + "groupId='" + this.groupId + '\'' + ", artifactId='"
				+ this.artifactId + '\'' + ", version='" + this.version + '\'' + ", releaseStatus='"
				+ this.releaseStatus + '\'' + ", refDocUrl='" + this.refDocUrl + '\''
				+ ", apiDocUrl='" + this.apiDocUrl + '\'' + ", repository=" + this.repository + '}';
	}
}

/**
 * @author Marcin Grzejszczak
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
class Repository {
	public String id;
	public String name;
	public String url;
	public Boolean snapshotsEnabled;

	@Override public String toString() {
		return "Repository{" + "id='" + this.id + '\'' + ", name='" + this.name + '\'' + ", url='"
				+ this.url + '\'' + ", snapshotsEnabled=" + this.snapshotsEnabled + '}';
	}
}