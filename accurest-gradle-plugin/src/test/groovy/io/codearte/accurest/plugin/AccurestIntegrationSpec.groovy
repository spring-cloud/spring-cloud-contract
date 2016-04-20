package io.codearte.accurest.plugin

import nebula.test.IntegrationSpec
import nebula.test.functional.GradleRunner

import java.nio.file.Files
import java.nio.file.Path

import static java.nio.charset.StandardCharsets.UTF_8

/**
 * @author Olga Maciaszek-Sharma 
 * @since 23.02.16
 */
class AccurestIntegrationSpec extends IntegrationSpec {

	public static final String SPOCK = "targetFramework = 'Spock'"
	public static final String JUNIT = "targetFramework = 'JUnit'"
	public static final String MVC_SPEC = "baseClassForTests = 'com.blogspot.toomuchcoding.MvcSpec'"
	public static final String MVC_TEST = "baseClassForTests = 'com.blogspot.toomuchcoding.MvcTest'"

	void setup() {
		classpathFilter = GradleRunner.CLASSPATH_ALL	//workaround for removing dependant modules by default filter
	}

	protected void switchToJunitTestFramework() {
		switchToJunitTestFramework(MVC_SPEC, MVC_TEST)
	}

	protected void switchToJunitTestFramework(String from, String to) {
		Path path = buildFile.toPath()
		String content = new StringBuilder(new String(Files.readAllBytes(path), UTF_8)).replaceAll(SPOCK, JUNIT)
				.replaceAll(from, to)
		Files.write(path, content.getBytes(UTF_8))
	}

}
