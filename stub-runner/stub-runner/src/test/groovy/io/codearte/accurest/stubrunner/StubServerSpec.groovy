package io.codearte.accurest.stubrunner

import spock.lang.Specification

class StubServerSpec extends Specification {
	static final int STUB_SERVER_PORT = 12180
	static final URL EXPECTED_URL = new URL("http://localhost:$STUB_SERVER_PORT")

	File repository = new File('src/test/resources/repository/mappings/com/ofg/bye')
	StubConfiguration stubConfiguration = new StubConfiguration("a:b")

	def 'should register stub mappings upon server start'() {
		given:
		List<WiremockMappingDescriptor> mappingDescriptors = new StubRepository(repository).getProjectDescriptors()
		StubServer pingStubServer = new StubServer(STUB_SERVER_PORT, stubConfiguration, mappingDescriptors, [])
		when:
		pingStubServer.start()
		then:
		"http://localhost:$pingStubServer.port/bye".toURL().text == 'Goodbye world!'
		cleanup:
		pingStubServer.stop()
	}

	def 'should provide stub server URL'() {
		given:
		List<WiremockMappingDescriptor> mappingDescriptors = new StubRepository(repository).getProjectDescriptors()
		StubServer pingStubServer = new StubServer(STUB_SERVER_PORT, stubConfiguration, mappingDescriptors, [])
		when:
		pingStubServer.start()
		then:
		pingStubServer.stubUrl == EXPECTED_URL
		cleanup:
		pingStubServer.stop()
	}
}
