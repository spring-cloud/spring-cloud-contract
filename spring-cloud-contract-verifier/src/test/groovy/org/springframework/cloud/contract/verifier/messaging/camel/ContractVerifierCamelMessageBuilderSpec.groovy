package org.springframework.cloud.contract.verifier.messaging.camel

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 * @since
 */
class ContractVerifierCamelMessageBuilderSpec extends Specification {
	def "should not throw an exception when headers are null"() {
		given:
			CamelContext camelContext = new DefaultCamelContext()
			ContractVerifierCamelMessageBuilder builder = new ContractVerifierCamelMessageBuilder(camelContext)
		when:
			builder.create(null, null)
		then:
			noExceptionThrown()
	}
}
