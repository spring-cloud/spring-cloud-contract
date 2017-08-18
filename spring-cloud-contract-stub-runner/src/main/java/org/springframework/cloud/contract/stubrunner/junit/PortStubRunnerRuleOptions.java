package org.springframework.cloud.contract.stubrunner.junit;

interface PortStubRunnerRuleOptions extends StubRunnerRuleOptions {

	/**
	 * Appends port to last added stub
	 */
	StubRunnerRule withPort(Integer port);

}
