package org.springframework.cloud.contract.verifier.builder

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Url
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

/**
 * A {@link JUnitMethodBodyBuilder} implementation that uses WebTestClient to send requests.
 *
 * @author Olga Maciaszek-Sharma
 *
 * @since 2.1.0
 */
@CompileStatic
@PackageScope
class WebTestClientJUnitMethodBodyBuilder extends RestAssuredJUnitMethodBodyBuilder {

    WebTestClientJUnitMethodBodyBuilder(Contract stubDefinition, ContractVerifierConfigProperties configProperties, String methodName) {
        super(stubDefinition, configProperties, methodName)
    }

    @Override
    protected String returnedResponseType() {
        return 'WebTestClientResponse'
    }

    @Override
    protected String returnedRequestType() {
        return 'WebTestClientRequestSpecification'
    }

    @Override
    protected void when(BlockBuilder bb) {
        bb.addLine(getInputString(request))
        bb.indent()

        Url url = getUrl(request)
        addQueryParameters(url, bb)
        addUrl(url, bb)
        addColonIfRequired(bb)
        bb.unindent()
    }
}
