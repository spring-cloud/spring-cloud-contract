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

package org.springframework.cloud.contract.stubrunner.spring.cloud;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.util.StringUtils;

/**
 * Maps Ivy based ids to service Ids. You might want to name the service you're calling
 * in another way than artifact id. If that's the case then this class should be used
 * to change do the proper mapping.
 *
 * Just provide in your properties file for example:
 *
 * stubrunner.idsToServiceIds:
 *     fraudDetectionServer: someNameThatShouldMapFraudDetectionServer
 *
 * @author Marcin Grzejszczak
 *
 * @since 1.0.0
 */
@ConfigurationProperties("stubrunner")
public class StubMapperProperties {

	/**
	 * Mapping of Ivy notation based ids to serviceIds
	 * inside your application
	 *
	 * Example
	 *
	 * "a:b"			->		"myService"
	 * "artifactId"		->		"myOtherService"
	 */
	private Map<String, String> idsToServiceIds = new HashMap<>();

	public Map<String, String> getIdsToServiceIds() {
		return this.idsToServiceIds;
	}

	public void setIdsToServiceIds(Map<String, String> idsToServiceIds) {
		this.idsToServiceIds = idsToServiceIds;
	}

	public String fromIvyNotationToId(String ivyNotation) {
		StubConfiguration stubConfiguration = new StubConfiguration(ivyNotation);
		String id = this.idsToServiceIds.get(ivyNotation);
		if (StringUtils.hasText(id)) {
			return id;
		}
		String groupAndArtifact = this.idsToServiceIds.get(stubConfiguration.getGroupId() +
				":" + stubConfiguration.getArtifactId());
		if (StringUtils.hasText(groupAndArtifact)) {
			return groupAndArtifact;
		}
		return this.idsToServiceIds.get(stubConfiguration.getArtifactId());
	}

	public String fromServiceIdToIvyNotation(String serviceId) {
		for (Map.Entry<String, String> entry : this.idsToServiceIds.entrySet()) {
			if (entry.getValue().equals(serviceId)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
