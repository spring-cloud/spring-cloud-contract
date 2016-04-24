package io.codearte.accurest.stubrunner.junit;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author Marcin Grzejszczak
 */
public class AccurestRuleJUnitTest {

	@BeforeClass
	@AfterClass
	public static void setupProps() {
			System.getProperties().setProperty("stubrunner.stubs.repository.root", "");
			System.getProperties().setProperty("stubrunner.stubs.classifier", "stubs");
	}

	// tag:classrule[]
	@ClassRule public static AccurestRule rule = new AccurestRule()
			.repoRoot(repoRoot())
			.downloadStub("io.codearte.accurest.stubs", "loanIssuance")
			.downloadStub("io.codearte.accurest.stubs:fraudDetectionServer");
	// end:classrule[]

	// tag:test[]
	@Test
	public void should_start_wiremock_servers() throws Exception {
		// expect: 'WireMocks are running'
			then(rule.findStubUrl("io.codearte.accurest.stubs", "loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isNotNull();
			then(rule.findStubUrl("loanIssuance")).isEqualTo(rule.findStubUrl("io.codearte.accurest.stubs", "loanIssuance"));
			then(rule.findStubUrl("io.codearte.accurest.stubs:fraudDetectionServer")).isNotNull();
		// and:
			then(rule.findAllRunningStubs().isPresent("loanIssuance")).isTrue();
			then(rule.findAllRunningStubs().isPresent("io.codearte.accurest.stubs", "fraudDetectionServer")).isTrue();
			then(rule.findAllRunningStubs().isPresent("io.codearte.accurest.stubs:fraudDetectionServer")).isTrue();
		// and: 'Stubs were registered'
			then(httpGet(rule.findStubUrl("loanIssuance").toString() + "/name")).isEqualTo("loanIssuance");
			then(httpGet(rule.findStubUrl("fraudDetectionServer").toString() + "/name")).isEqualTo("fraudDetectionServer");
	}
	// end:test[]

	private static String repoRoot()  {
		try {
			return AccurestRuleJUnitTest.class.getResource("/m2repo/repository/").toURI().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private String httpGet(String url) throws Exception {
		try(InputStream stream = URI.create(url).toURL().openStream()) {
			return IOUtils.toString(stream);
		}
	}
}
