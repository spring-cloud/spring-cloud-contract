package org.springframework.cloud.stream.sample.verifier.source;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ContractVerifierSampleStreamSourceApplication.class)
public class ContractVerifierSampleStreamSourceApplicationTests {

	@Test
	public void contextLoads() {
	}

}
