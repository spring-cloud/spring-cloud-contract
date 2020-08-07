package contracts;

import contracts.RestBase;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierObjectMapper;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.fileToBytes;

@SuppressWarnings("rawtypes")
public class MessagingTest extends RestBase {
	@Inject ContractVerifierMessaging contractVerifierMessaging;
	@Inject ContractVerifierObjectMapper contractVerifierObjectMapper;

	@Test
	public void validate_pingPong() throws Exception {
		// when:
			triggerMessage("ping_pong", "output");

		// then:
			ContractVerifierMessage response = contractVerifierMessaging.receive("output");
			assertThat(response).isNotNull();

		// and:
			DocumentContext parsedJson = JsonPath.parse(contractVerifierObjectMapper.writeValueAsString(response.getPayload()));
			assertThatJson(parsedJson).field("['message']").isEqualTo("pong");
	}

}
