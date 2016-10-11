package org.springframework.cloud.contract.stubrunner

import groovy.transform.PackageScope

/**
 * @author Marcin Grzejszczak
 */
@PackageScope class TestStubRunnerOptions extends StubRunnerOptions {
	public TestStubRunnerOptions() {
		super(1, 2, "", false, "", new ArrayList<StubConfiguration>(),
				new HashMap<StubConfiguration, Integer>());
	}
}
