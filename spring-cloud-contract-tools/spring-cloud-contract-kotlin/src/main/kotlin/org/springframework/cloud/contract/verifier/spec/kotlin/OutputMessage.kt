package org.springframework.cloud.contract.verifier.spec.kotlin

import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.OutputMessage

class OutputMessage(contract: Contract) {
    init {
        contract.outputMessage = OutputMessage()
    }
}