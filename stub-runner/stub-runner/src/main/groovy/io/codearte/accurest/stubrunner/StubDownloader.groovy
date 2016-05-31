package io.codearte.accurest.stubrunner

interface StubDownloader {

	/**
	 * Returns a mapping of updated StubConfiguration (it will contain the resolved version) and the location of the downloaded JAR.
	 * If there was no artifact this method will return {@code null}.
	 */
	Map.Entry<StubConfiguration,File> downloadAndUnpackStubJar(StubRunnerOptions options, StubConfiguration stubConfiguration)


}