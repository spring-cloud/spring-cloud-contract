package io.codearte.accurest.stubrunner.spring.cloud;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps Ivy based ids to service Ids. You might want to name the service you're calling
 * in another way than artifact id. If that's the case then this class should be used
 * to change do the proper mapping.
 *
 * Just provide in your properties file for example:
 *
 * stubrunner.stubs.idsToServiceIds:
 *     ivyNotation: someValueInsideYourCode
 *     fraudDetectionServer: someNameThatShouldMapFraudDetectionServer
 *
 * @author Marcin Grzejszczak
 */
@ConfigurationProperties("stubrunner.stubs")
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
		return idsToServiceIds.get(ivyNotation);
	}

	public String fromServiceIdToIvyNotation(String serviceId) {
		for (Map.Entry<String, String> entry : idsToServiceIds.entrySet()) {
			if (entry.getValue().equals(serviceId)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
