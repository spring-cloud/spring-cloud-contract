package org.springframework.cloud.stream.verifier.sample.sink;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ContractVerifierSampleStreamSinkApplication.class)
public class ContractVerifierSampleStreamSinkApplicationTests {

	@Test
	public void contextLoads() {
	}

}
