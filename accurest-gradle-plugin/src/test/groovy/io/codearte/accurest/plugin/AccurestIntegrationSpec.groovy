package io.codearte.accurest.plugin

import nebula.test.IntegrationSpec

import java.nio.file.Files
import java.nio.file.Path

import static java.nio.charset.StandardCharsets.UTF_8
import static java.nio.charset.StandardCharsets.UTF_8

/**
 * @author Olga Maciaszek-Sharma 
 @since 23.02.16
 */
class AccurestIntegrationSpec extends IntegrationSpec{

	public static final String SPOCK = "targetFramework = 'Spock'"
	public static final String JUNIT = "targetFramework = 'JUnit'"
	public static final String MVC_SPEC = "baseClassForTests = 'com.blogspot.toomuchcoding.MvcSpec'"
	public static final String MVC_TEST = "baseClassForTests = 'com.blogspot.toomuchcoding.MvcTest'"

	protected void switchToJunitTestFramework() {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll(SPOCK, JUNIT)
				.replaceAll(MVC_SPEC, MVC_TEST)
		Files.write(path, content.getBytes(UTF_8))
	}

}
