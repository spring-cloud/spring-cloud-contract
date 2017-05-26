package org.springframework.cloud.contract.verifier.messaging.stream

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class ContractVerifierHelperForStreamTest extends Specification {

	def 'should throw exception when a null payload was sent'() {
		given:
			ContractVerifierHelper helper = new ContractVerifierHelper(null)
		when:
			helper.convert(null)
		then:
			IllegalArgumentException e = thrown(IllegalArgumentException)
			e.message.contains("Message must not be null")
	}
}
